#!/usr/bin/env python3
"""Reproducible isolated build; installed/shared binaries are read-only inputs."""
from pathlib import Path
import hashlib,json,os,subprocess,zipfile
from release_pipeline import config, filename
ROOT=Path(__file__).resolve().parents[1]
JDK=Path(os.environ.get('AR_JAVA_HOME') or os.environ['JAVA_HOME'])
SERVER=Path(os.environ.get('AR_SERVER_JAR') or os.environ['HYTALE_SERVER_JAR'])
PIN=config(ROOT/'release.json')['sdkSha256']
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def run(args):subprocess.run(list(map(str,args)),check=True)
assert sha(SERVER)==PIN,'Reverify changed server before building'
run([os.sys.executable,ROOT/'tools/validate_content.py'])
classes=ROOT/'build/classes';tests=ROOT/'build/test-classes';reports=ROOT/'build/reports'
for p in [classes,tests,reports]:p.mkdir(parents=True,exist_ok=True)
for p in [classes,tests]:
 for f in p.rglob('*.class'):f.unlink()
core=sorted((ROOT/'src/main/java/dev/astralreach/core').glob('*.java'));test=sorted((ROOT/'src/test/java').rglob('*.java'));native=sorted((ROOT/'src/main/java/dev/astralreach/hytale').glob('*.java'))
run([JDK/'bin/javac','--release','25','-Xlint:all','-d',classes,*core]);run([JDK/'bin/javac','--release','25','-Xlint:all','-cp',classes,'-d',tests,*test])
result=subprocess.check_output([str(JDK/'bin/java'),'-ea','-cp',str(classes)+os.pathsep+str(tests),'dev.astralreach.core.CoreTests'],text=True);(reports/'core.txt').write_text(result);print(result.strip())
run([JDK/'bin/javac','--release','25','-Xlint:all','-cp',str(classes)+os.pathsep+str(SERVER),'-d',classes,*native])
assert (ROOT/'src/main/resources/LICENSE').read_bytes()==(ROOT/'LICENSE').read_bytes(),'Packaged license must match the repository license'
out=ROOT/'build/libs'/filename(config(ROOT/'release.json'));out.parent.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as z:
 for folder in [classes,ROOT/'src/main/resources']:
  for p in sorted(folder.rglob('*')):
   if p.is_file():
    info=zipfile.ZipInfo(p.relative_to(folder).as_posix(),(2026,10,23,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=0o100644<<16;z.writestr(info,p.read_bytes())
record={'artifact':str(out.relative_to(ROOT)),'sha256':sha(out),'bytes':out.stat().st_size,'server_sha256':PIN,'java_release':25,'native_observed':False,'installed':False,'source_sha256':{str(p.relative_to(ROOT)):sha(p) for p in core+native}}
(reports/'build.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps({k:v for k,v in record.items() if k!='source_sha256'}))
