#!/usr/bin/env python3
"""Client-facing animation contracts not parsed by the headless asset loader."""
from pathlib import Path
import json,re

ROOT=Path(__file__).resolve().parents[1]/'src/main/resources'
TRACKS={'position','orientation','shapeStretch','shapeVisible','shapeUvOffset'}

def names(node):
 yield node['name']
 for child in node.get('children',[]):yield from names(child)

for path in (ROOT/'Common/UI/Custom/Pages/AstralReach').glob('*.ui'):
 for literal in re.finditer(r'"(?:\\.|[^"\\])*"',path.read_text()):
  for escape in re.finditer(r'\\(.)',literal.group()):
   assert escape.group(1) in ['\\','"'],(str(path),'unsupported static UI escape',escape.group())

checked=set()
def clip_contract(clip,model):
 nodes={name for root in json.loads((ROOT/'Common'/model).read_text())['nodes'] for name in names(root)}
 data=json.loads((ROOT/'Common'/clip).read_text())
 assert data['duration']>0 and isinstance(data['holdLastKeyframe'],bool),clip
 for target,tracks in data['nodeAnimations'].items():
  assert target in nodes,(clip,'unknown target',target)
  assert TRACKS<=tracks.keys(),(clip,'missing client track arrays',TRACKS-tracks.keys())
  for key in TRACKS:
   frames=tracks[key];assert isinstance(frames,list),(clip,key)
   times=[frame['time'] for frame in frames]
   assert times==sorted(times) and all(0<=t<=data['duration'] for t in times),(clip,key,'invalid times')
 checked.add(clip)
for path in (ROOT/'Server/Item/Items/AstralReach').glob('*.json'):
 item=json.loads(path.read_text());block=item.get('BlockType',{})
 for definition in [block,*block.get('State',{}).get('Definitions',{}).values()]:
  clip=definition.get('CustomModelAnimation')
  if not clip:continue
  model=definition.get('CustomModel',block['CustomModel'])
  clip_contract(clip,model)
for path in (ROOT/'Server/Models/AstralReach').glob('*.json'):
 model=json.loads(path.read_text())
 for animation_set in model.get('AnimationSets',{}).values():
  for animation in animation_set['Animations']:clip_contract(animation['Animation'],model['Model'])
print(f'Client animation contract: {len(checked)} referenced clips have real targets and all five track arrays; native playback still required.')
