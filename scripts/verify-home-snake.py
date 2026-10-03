import subprocess,json
from pathlib import Path
adb='/Users/vidyasagar/Library/Android/sdk/platform-tools/adb'
repo=Path(__file__).resolve().parents[1]
out=repo/'assets/hush-1.0.4-fixes-proof';out.mkdir(parents=True,exist_ok=True)
def cmd(*args):return subprocess.check_output([adb,'-s','emulator-5558',*args],text=True)
metadata_path=repo/'app/build/outputs/apk/debug/output-metadata.json'
metadata=json.loads(metadata_path.read_text())
arm64=next(item for item in metadata['elements'] if any(f.get('value')=='arm64-v8a' for f in item['filters']))
cmd('install','-r',str(metadata_path.parent/arm64['outputFile']))
cmd('install','-r',str(repo/'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'))
results=[]
try:
 for name,size,density,font in [('mobile','1080x2400','420','1.0'),('tablet-landscape','2560x1600','320','1.0'),('tablet-portrait','1600x2560','320','1.0'),('mobile-large-text','1080x2400','420','2.0')]:
  cmd('shell','am','force-stop','com.vidsagar.hush.debug');cmd('shell','wm','size',size);cmd('shell','wm','density',density);cmd('shell','settings','put','system','font_scale',font)
  tests='org.schabi.newpipe.hush.HomeSpacingTest,org.schabi.newpipe.hush.games.GameSmoothnessTest#snakeCrossesTheEdgeWithoutAnimatingThroughTheBoard'
  folder=out/name;folder.mkdir(exist_ok=True)
  with (folder/'tests.log').open('w') as log:
   subprocess.run([adb,'-s','emulator-5558','shell','am','instrument','-w','-r','-e','class',tests,'com.vidsagar.hush.debug.test/androidx.test.runner.AndroidJUnitRunner'],stdout=log,stderr=subprocess.STDOUT,check=True,timeout=120)
  passed='OK (2 tests)' in (folder/'tests.log').read_text()
  if not passed:print((folder/'tests.log').read_text(),flush=True);raise AssertionError(name)
  (folder/'home.png').write_bytes(subprocess.check_output([adb,'-s','emulator-5558','exec-out','run-as','com.vidsagar.hush.debug','cat','files/fixes-proof/home.png']))
  results.append({'window':name,'passed':passed});print(results[-1],flush=True)
  (out/'results.json').write_text(json.dumps(results,indent=2)+'\n')
finally:
 cmd('shell','wm','size','reset');cmd('shell','wm','density','reset');cmd('shell','settings','put','system','font_scale','1.0')
