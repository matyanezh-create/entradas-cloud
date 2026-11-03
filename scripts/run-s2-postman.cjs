const fs=require('fs');
const path=require('path');
const newman=require('newman');
const root=path.resolve(__dirname,'..');
const working=path.resolve(process.argv[2] || path.join(root,'resumenes-locales','postman'));
fs.mkdirSync(working,{recursive:true});
const collection=JSON.parse(fs.readFileSync(path.join(root,'postman/Semana2_S3.postman_collection.json'),'utf8'));
newman.run({collection,envVar:[{key:'eventosUrl',value:process.argv[3] || 'http://127.0.0.1:8081'},{key:'comprasUrl',value:process.argv[4] || 'http://127.0.0.1:8082'}],workingDir:working,insecure:false,timeoutRequest:20000,reporters:['cli']})
.on('request',(err,args)=>{
 if(err)return;
 if(args.item.name==='03 Generar archivo fisico'&&args.response.code===201){
  const text=args.response.stream.toString('utf8');
  fs.writeFileSync(path.join(working,'resumen-generado.txt'),text);
  fs.writeFileSync(path.join(working,'resumen-corregido.txt'),text+'\nObservacion: referencia de contacto corregida\n');
  fs.writeFileSync(path.join(working,'resumen-numero-incorrecto.txt'),'RESUMEN DE COMPRA\nNumero: 999999999\n');
 }
})
.on('done',(err,summary)=>{
 if(err){console.error(String(err));process.exitCode=1;return;}
 const result={stats:summary.run.stats,failures:summary.run.failures.map(x=>({name:x.error?.name,message:x.error?.message,source:x.source?.name})),requests:summary.run.executions.map(x=>({name:x.item.name,status:x.response?.code,assertions:x.assertions.map(a=>({name:a.assertion,passed:!a.error}))}))};
 fs.writeFileSync(path.join(working,'resultado-postman.json'),JSON.stringify(result,null,2));
 process.exitCode=result.failures.length?1:0;
});
