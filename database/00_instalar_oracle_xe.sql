-- Requiere una sesion SYSTEM ya autenticada contra XEPDB1.
-- El asistente PowerShell inicia SQL Plus con -L antes de cargar este archivo.
set echo off
set verify off
set feedback on
set define on
whenever oserror exit failure rollback
whenever sqlerror exit failure rollback
begin
  if sys_context('USERENV','SESSION_USER') <> 'SYSTEM' then
    raise_application_error(-20000,'Ejecuta este instalador conectado como SYSTEM.');
  end if;
  if sys_context('USERENV','CON_NAME') = 'CDB$ROOT' then
    raise_application_error(-20001,'Conecta a XEPDB1, no al contenedor raiz XE.');
  end if;
end;
/
declare
  cantidad number;
begin
  select count(*) into cantidad from dba_users where username in ('EVENTOS_APP','COMPRAS_APP');
  if cantidad > 0 then
    raise_application_error(-20002,'Ya existe EVENTOS_APP o COMPRAS_APP. No se borro nada. Revisa los esquemas antes de continuar.');
  end if;
end;
/
prompt Elige contrasenas distintas de 16 a 30 caracteres.
prompt Usa solo letras, numeros y guion bajo, empezando con una letra.
accept eventos_pass char prompt 'Nueva contrasena EVENTOS_APP (oculta): ' hide
accept compras_pass char prompt 'Nueva contrasena COMPRAS_APP (oculta): ' hide
create user EVENTOS_APP identified by "&&eventos_pass" default tablespace USERS quota 20M on USERS;
undefine eventos_pass
grant create session, create table, create sequence to EVENTOS_APP;
create user COMPRAS_APP identified by "&&compras_pass" default tablespace USERS quota 20M on USERS;
undefine compras_pass
grant create session, create table, create sequence to COMPRAS_APP;
-- SYSTEM conserva sus permisos, y los objetos se crean en cada esquema.
alter session set current_schema = EVENTOS_APP;
@@01_eventos.sql
alter session set current_schema = COMPRAS_APP;
@@02_compras.sql
alter session set current_schema = SYSTEM;
prompt Comprobando objetos instalados:
select owner, object_name, object_type from dba_objects
where owner in ('EVENTOS_APP','COMPRAS_APP') and object_type in ('TABLE','SEQUENCE')
order by owner, object_type, object_name;
prompt Instalacion terminada. Inicia scripts/run-oracle-local.ps1 con las dos contrasenas elegidas.
exit success
