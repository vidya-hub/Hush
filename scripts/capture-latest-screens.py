"""Run native overlay/navigation checks and capture mobile/tablet README images."""
from pathlib import Path
import json,os,subprocess
repo=Path(__file__).resolve().parents[1]
adb=os.environ.get('ADB',str(Path.home()/'Library/Android/sdk/platform-tools/adb'))
serial=os.environ.get('ANDROID_SERIAL','emulator-5558')
pkg='com.vidsagar.hush.debug'
real=os.environ.get('HUSH_REAL_VIDEO')=='1'
showcase=os.environ.get('HUSH_GAMEPLAY')=='1'
output=repo/('assets/hush-gameplay-proof' if showcase else 'assets/hush-live-overlay-proof' if real else 'assets/hush-overlay-proof');output.mkdir(parents=True,exist_ok=True)
def cmd(*args):return subprocess.check_output([adb,'-s',serial,*args],text=True)
size=cmd('shell','wm','size');density=cmd('shell','wm','density');font=cmd('shell','settings','get','system','font_scale').strip()
results=[]
try:
 cmd('install','-r',str(repo/'app/build/outputs/apk/debug/Hush_5.3.1-arm64-v8a-debug.apk'))
 cmd('install','-r',str(repo/'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'))
 for name,pixels,dpi in [('tablet-landscape','2560x1600','320'),('tablet-portrait','1600x2560','320'),('mobile','1080x2400','420')]:
  cmd('shell','am','force-stop',pkg);cmd('shell','wm','size',pixels);cmd('shell','wm','density',dpi);cmd('shell','settings','put','system','font_scale','1.0')
  capture_dir='files/game-showcase' if showcase else 'files/redesign-proof'
  cmd('shell','run-as',pkg,'rm','-rf',capture_dir);folder=output/name;folder.mkdir(exist_ok=True)
  tests='org.schabi.newpipe.hush.TabletWindowTest,org.schabi.newpipe.hush.RedesignNavigationTest#homeRoutesAndLibraryTitles,org.schabi.newpipe.hush.TabletLocalPlaybackTest'
  if real:tests='org.schabi.newpipe.hush.RedesignPlaybackTest'
  if showcase:tests='org.schabi.newpipe.hush.games.GameShowcaseCaptureTest'
  tests=os.environ.get('HUSH_CAPTURE_TESTS',tests)
  print('Testing '+name,flush=True)
  with (folder/'tests.log').open('w') as log:
   subprocess.run([adb,'-s',serial,'shell','am','instrument','-w','-r','-e','class',tests,pkg+'.test/androidx.test.runner.AndroidJUnitRunner'],stdout=log,stderr=subprocess.STDOUT,timeout=300,check=True)
  passed='\nOK (' in (folder/'tests.log').read_text()
  for filename in cmd('shell','run-as',pkg,'ls',capture_dir).split():
   if filename.endswith('.png'):(folder/filename).write_bytes(subprocess.check_output([adb,'-s',serial,'exec-out','run-as',pkg,'cat',capture_dir+'/'+filename]))
  results.append({'configuration':name,'passed':passed,'captures':len(list(folder.glob('*.png')))})
  (output/'results.json').write_text(json.dumps(results,indent=2)+'\n');print(results[-1],flush=True)
finally:
 for option,original in [('size',size),('density',density)]:
  override=[line.split(':',1)[1].strip() for line in original.splitlines() if line.startswith('Override')]
  cmd('shell','wm',option,override[0] if override else 'reset')
 cmd('shell','settings','put','system','font_scale',font)
 cmd('shell','am','start','-n',pkg+'/org.schabi.newpipe.MainActivity')
if len(results)!=3 or not all(r['passed'] for r in results):raise SystemExit('Overlay regression failed; inspect tests.log')
