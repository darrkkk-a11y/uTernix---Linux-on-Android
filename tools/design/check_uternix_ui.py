#!/usr/bin/env python3
"""Validate sources after uTernix vector/icon theme migration; standard library only."""
from pathlib import Path
import re, sys, xml.etree.ElementTree as ET
root = Path(__file__).resolve().parents[2]
res = root/'tessera-linux/app/src/main/res'
ui = root/'tessera-linux/app/src/main/java/com/daryan/tessera/ui'
errors=[]
xml_count=0
for file in res.rglob('*.xml'):
    try: ET.parse(file); xml_count+=1
    except ET.ParseError as e: errors.append(f'XML {file}: {e}')
icons=list((res/'drawable').glob('ut_*.xml'))
if len(icons) != 460: errors.append(f'Expected 460 Spectrum icons, found {len(icons)}')
for file in icons:
    r=ET.parse(file).getroot()
    if r.tag!='vector': errors.append(f'Not a vector: {file}')
    if not any(t.tag=='path' for t in r.iter()):errors.append(f'No vector paths: {file}')
all_code='\n'.join(f.read_text(encoding='utf-8') for f in ui.rglob('*.kt'))
for match in set(re.findall(r'R\.drawable\.(ut_[a-z0-9_]+)',all_code)):
    if not (res/'drawable'/f'{match}.xml').exists():errors.append(f'Missing drawable: {match}')
if re.search(r'R\.drawable\.debv?_',all_code):errors.append('Legacy raster/vector icon resource referenced')
if '.glass(' in all_code or 'TabPills(' in all_code:errors.append('Legacy glass or upper tab UI still linked')
if not (ui/'UternixHome.kt').exists():errors.append('uTernix Home missing')
if not (ui/'UternixNavigation.kt').exists():errors.append('uTernix navigation missing')
for f in ['DebTheme.kt','Glass.kt','FloatingDock.kt']:
    if (ui/'deb'/f).exists() or (ui/f).exists():errors.append(f'Legacy frontend file still exists: {f}')
print(f'XML files parsed: {xml_count}; uTernix Android vectors: {len(icons)}')
if errors:
    for e in errors:print('FAIL',e)
    sys.exit(1)
print('PASS: static UI/resource checks (not an Android compile)')
