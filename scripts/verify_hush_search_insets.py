#!/usr/bin/env python3
"""With search focused and the keyboard visible, verify the real app keeps its chrome on screen."""
import argparse
import re
import subprocess
import time
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument('--serial', required=True)
parser.add_argument('--package', default='com.vidsagar.hush.debug')
args = parser.parse_args()
adb = ['adb', '-s', args.serial]

def command(*words):
    return subprocess.check_output(adb + list(words), stderr=subprocess.STDOUT, text=True)

for attempt in range(5):
    path = '/sdcard/hush-insets-' + str(time.time_ns()) + '.xml'
    result = command('shell', 'uiautomator', 'dump', path)
    if 'UI hierchary dumped' in result:
        root = ET.fromstring(command('shell', 'cat', path))
        command('shell', 'rm', path)
        break
    time.sleep(.4)
else:
    raise RuntimeError('Unable to inspect current UI')

for resource in ['home_search_edit_text', 'home_header', 'home_incognito']:
    node = next((n for n in root.iter('node')
                 if n.get('resource-id') == args.package + ':id/' + resource), None)
    assert node is not None, resource + ' is missing from the visible UI'
    x1, y1, x2, y2 = map(int, re.findall(r'\d+', node.get('bounds')))
    assert x2 > x1 and y2 > y1 and y1 >= 0, resource + ' is obscured: ' + node.get('bounds')
    print(resource, node.get('bounds'))
print('PASS: Search, header, and Incognito remain visible with the keyboard open.')
