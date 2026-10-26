#!/usr/bin/env python3
"""Original source-authored native UI decoration; real controls remain native widgets."""
from pathlib import Path
import math,random
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1]/'src/main/resources/Common/UI/Custom/Pages/AstralReach/Art';R.mkdir(parents=True,exist_ok=True)
PALETTES={'Sol':((238,182,65),(255,231,132)),'Orbis':((49,119,158),(107,154,79)),'Lune':((141,146,154),(101,107,119)),'Rustreach':((156,84,48),(100,54,39)),'Nacre':((125,192,209),(190,228,231)),'Vesper':((192,157,69),(143,111,48)),'Umbra':((112,87,149),(155,133,183))}
# Finer broad voxel relief; the same bounding circle and original family palette.
for name,(a,b) in PALETTES.items():
 size=160;scale=3;im=Image.new('RGBA',(size*scale,size*scale));d=ImageDraw.Draw(im);faces=[];radius=9.2;unit=6.8
 voxels={(x,y,z) for x in range(-9,10) for y in range(-9,10) for z in range(-9,10) if x*x+y*y+z*z<radius*radius}
 if name=='Lune':
  craters=[(-2,2,8.4,2.3),(3,-2,8.1,2.5),(-6,3,5.6,1.9),(0,8,3.5,1.7)]
  voxels={p for p in voxels if not any(sum((p[i]-c[i])**2 for i in range(3))<c[3]**2 for c in craters)}
 yaw=.45;pitch=.23
 def project(p):
  x,y,z=p;xx=x*math.cos(yaw)+z*math.sin(yaw);zz=-x*math.sin(yaw)+z*math.cos(yaw);yy=y*math.cos(pitch)-zz*math.sin(pitch);depth=y*math.sin(pitch)+zz*math.cos(pitch)
  return ((size/2+xx*unit)*scale,(size/2-yy*unit)*scale,depth)
 data=[((-1,0,0),[(-.5,-.5,-.5),(-.5,.5,-.5),(-.5,.5,.5),(-.5,-.5,.5)],.68),((0,1,0),[(-.5,.5,-.5),(.5,.5,-.5),(.5,.5,.5),(-.5,.5,.5)],1.09),((0,0,1),[(-.5,-.5,.5),(.5,-.5,.5),(.5,.5,.5),(-.5,.5,.5)],.91)]
 for x,y,z in voxels:
  for (dx,dy,dz),corners,shade in data:
   if (x+dx,y+dy,z+dz) in voxels:continue
   p=[project((x+cx,y+cy,z+cz)) for cx,cy,cz in corners];sx,sy,sz=x/1.77,y/1.77,z/1.77;f=math.sin(sx*.85+sz*.53)+math.cos(sy*.9-sz*.71)+.4*math.sin(sx+sy)
   color=b if f>.9 else a;factor=shade*(.94+.055*math.sin(sx*.5+sy*.3+sz*.6));color=tuple(min(255,max(0,int(c*factor))) for c in color)+(255,)
   faces.append((sum(q[2] for q in p)/4,[(q[0],q[1]) for q in p],color))
 for _,p,c in sorted(faces,key=lambda q:q[0]):d.polygon(p,fill=c)
 im.resize((size,size),Image.Resampling.LANCZOS).save(R/f'Planet_{name}.png')

def panel(name,top,bottom,edge='#476780',cut=4):
 w,h=96,64;im=Image.new('RGBA',(w,h));d=ImageDraw.Draw(im);t=tuple(bytes.fromhex(top[1:]));b=tuple(bytes.fromhex(bottom[1:]));poly=[(cut,0),(w-cut-1,0),(w-1,cut),(w-1,h-cut-1),(w-cut-1,h-1),(cut,h-1),(0,h-cut-1),(0,cut)]
 for y in range(h):
  c=tuple(round(t[i]+(b[i]-t[i])*y/(h-1)) for i in range(3));d.line((0,y,w,y),fill=c)
 mask=Image.new('L',(w,h));ImageDraw.Draw(mask).polygon(poly,fill=255);im.putalpha(mask);d=ImageDraw.Draw(im);d.line(poly+[poly[0]],fill='#080f18',width=2);d.line([(cut+1,2),(w-cut-2,2),(w-3,cut+1)],fill=edge,width=2);d.line([(2,cut+1),(2,h-cut-2)],fill=edge,width=1);d.line([(cut+2,h-4),(w-cut-3,h-4),(w-4,h-cut-3)],fill='#07101a',width=2);im.save(R/name)
for args in [('WindowPatch.png','#183049','#0b1723','#6686a0'),('PanelPatch.png','#101f2e','#0b1721','#385d79'),('TitleHeader.png','#244d76','#122a46','#6687a9'),('SectionHeader.png','#1c405c','#10273c','#4d7694'),('Row.png','#122432','#0a1722','#355773'),('RowHovered.png','#1b3d4b','#102936','#7bc4cb'),('RowPressed.png','#10232d','#0b1b24','#5fadb8'),('ControlPrimary.png','#0868da','#0340a4','#56a7ff'),('ControlPrimary_Hovered.png','#1685f2','#0754b9','#93cdff'),('ControlPrimary_Pressed.png','#104f99','#05336d','#3881c2'),('ControlSecondary.png','#354a64','#202e43','#6c88a5'),('ControlSecondary_Hovered.png','#456079','#2d435c','#9db7ce'),('ControlSecondary_Pressed.png','#25364b','#172738','#4b7895'),('ControlDisabled.png','#2b3743','#202b36','#475869')]:panel(*args)
im=Image.new('RGBA',(64,64),(32,107,122,30));d=ImageDraw.Draw(im);d.rectangle((1,1,62,62),outline='#62e5dd',width=3);d.line((5,6,58,6),fill='#3e8e9a',width=2);im.save(R/'SelectedFrame.png')
im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im);d.ellipse((3,3,124,124),outline='#78e4da',width=3);im.save(R/'SelectionRing.png')
im=Image.new('RGBA',(48,48));d=ImageDraw.Draw(im);d.rectangle((2,2,45,45),outline='#5de2db',width=3);d.line((10,24,20,34,39,12),fill='#69e8de',width=5);im.save(R/'Ready.png')
im=Image.new('RGBA',(48,48));d=ImageDraw.Draw(im);d.arc((12,3,36,31),180,360,fill='#8ca5ba',width=5);d.rectangle((8,20,40,43),fill='#7b92aa');d.ellipse((21,27,27,33),fill='#15293c');d.rectangle((23,31,25,37),fill='#15293c');im.save(R/'Lock.png')
for name,kind in [('Compass.png','compass'),('RocketGlyph.png','rocket'),('OrbitGlyph.png','orbit'),('CloseGlyph.png','close'),('CanisterGlyph.png','canister')]:
 im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im);color='#bdd8ef'
 if kind=='compass':
  d.ellipse((22,22,42,42),outline=color,width=3)
  for poly in [[(32,1),(23,25),(32,20),(41,25)],[(32,63),(23,39),(32,44),(41,39)],[(1,32),(25,23),(20,32),(25,41)],[(63,32),(39,23),(44,32),(39,41)]]:d.polygon(poly,fill=color)
 elif kind=='rocket':
  d.polygon([(32,4),(22,20),(22,46),(42,46),(42,20)],fill=color);d.ellipse((27,20,37,30),fill='#17324b');d.polygon([(22,34),(14,46),(14,54),(24,48)],fill=color);d.polygon([(42,34),(50,46),(50,54),(40,48)],fill=color);d.polygon([(26,51),(32,63),(38,51)],fill=color)
 elif kind=='canister':
  d.rectangle((22,7,42,12),fill=color);d.rectangle((18,15,46,55),outline=color,width=4);d.rectangle((23,25,41,40),fill='#63dfda')
 elif kind=='orbit':d.ellipse((14,14,50,50),outline=color,width=3);d.ellipse((3,23,61,41),outline=color,width=3)
 else:d.line((13,13,51,51),fill=color,width=8);d.line((51,13,13,51),fill=color,width=8)
 im.save(R/name)
# Native ProgressBar handles the actual server Value; these are decorative textures only.
for name,top,bottom in [('MeterFill.png',(96,232,225),(27,158,195)),('MeterTrack.png',(20,35,47),(9,21,32))]:
 im=Image.new('RGBA',(256,16));d=ImageDraw.Draw(im)
 for y in range(16):d.line((0,y,255,y),fill=tuple(round(top[i]+(bottom[i]-top[i])*y/15) for i in range(3)))
 d.rectangle((0,0,255,15),outline='#4b7890',width=1);im.save(R/name)
im=Image.new('RGB',(640,544),'#07131e');d=ImageDraw.Draw(im);rng=random.Random(84211)
for _ in range(100):
 x,y=rng.randrange(640),rng.randrange(544);d.point((x,y),fill=rng.choice(['#233647','#405569','#1c3142']))
for rx,ry in [(180,135),(280,220),(390,305),(500,390)]:d.ellipse((60-rx,300-ry,60+rx,300+ry),outline='#314c61',width=1)
# A sparse original belt of painted fragments follows the system chart's inner orbital arc.
for i in range(30):
 t=-1.3+i*.087;x=60+217*math.cos(t);y=300+168*math.sin(t);size=rng.choice([2,3,4]);d.polygon([(x-size,y),(x,y-size),(x+size,y),(x,y+size)],fill=rng.choice(['#626267','#797a7c','#4c505a']))
sun=Image.open(R/'Planet_Sol.png').resize((280,280));im.paste(sun,(-140,155),sun);im.save(R/'SystemChart.png')
print('Rendered original voxel relief, native nine-slice frames, glyphs and teal meter art; loaded comparison pending.')
im=Image.new('RGB',(640,400),'#091622');d=ImageDraw.Draw(im);rng=random.Random(9440)
for _ in range(90):d.point((rng.randrange(640),rng.randrange(400)),fill=rng.choice(['#264258','#344c62','#1b3447']))
im.save(R/'StarField.png')
