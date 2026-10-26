from pathlib import Path
import math,random
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1]/'src/main/resources/Common/UI/Custom/Pages/AstralReach/Art';R.mkdir(exist_ok=True)
# Original source-rendered low-poly globes; no generated mock-up is pasted over native controls.
PALETTES={'Sol':((238,182,65),(255,231,132)),'Orbis':((49,119,158),(107,154,79)),'Lune':((141,146,154),(101,107,119)),'Rustreach':((156,84,48),(100,54,39)),'Nacre':((125,192,209),(190,228,231)),'Vesper':((192,157,69),(143,111,48)),'Umbra':((112,87,149),(155,133,183))}
for name,(a,b) in PALETTES.items():
 size=160;scale=3;im=Image.new('RGBA',(size*scale,size*scale));d=ImageDraw.Draw(im);faces=[];radius=5.2
 voxels={(x,y,z) for x in range(-5,6) for y in range(-5,6) for z in range(-5,6) if x*x+y*y+z*z<radius*radius}
 yaw=.45;pitch=.23
 def project(p):
  x,y,z=p;xx=x*math.cos(yaw)+z*math.sin(yaw);zz=-x*math.sin(yaw)+z*math.cos(yaw);yy=y*math.cos(pitch)-zz*math.sin(pitch);depth=y*math.sin(pitch)+zz*math.cos(pitch)
  return ((size/2+xx*12)*scale,(size/2-yy*12)*scale,depth)
 data=[((-1,0,0),[(-.5,-.5,-.5),(-.5,.5,-.5),(-.5,.5,.5),(-.5,-.5,.5)],.68),((0,1,0),[(-.5,.5,-.5),(.5,.5,-.5),(.5,.5,.5),(-.5,.5,.5)],1.09),((0,0,1),[(-.5,-.5,.5),(.5,-.5,.5),(.5,.5,.5),(-.5,.5,.5)],.91)]
 for x,y,z in voxels:
  for (dx,dy,dz),corners,shade in data:
   if (x+dx,y+dy,z+dz) in voxels:continue
   p=[project((x+cx,y+cy,z+cz)) for cx,cy,cz in corners];f=math.sin(x*.85+z*.53)+math.cos(y*.9-z*.71)+.4*math.sin(x+y)
   color=b if f>.9 else a
   # Broad material planes and slight painted tonal variation, not a noisy checker texture.
   factor=shade*(.94+.055*math.sin(x*.5+y*.3+z*.6));color=tuple(min(255,max(0,int(c*factor))) for c in color)+(255,)
   faces.append((sum(q[2] for q in p)/4,[(q[0],q[1]) for q in p],color))
 for _,p,c in sorted(faces,key=lambda q:q[0]):d.polygon(p,fill=c)
 im.resize((size,size),Image.Resampling.LANCZOS).save(R/f'Planet_{name}.png')
im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im);d.rectangle((1,1,62,62),outline='#78d0c7',width=2);d.line((5,6,58,6),fill='#335f70',width=2);im.save(R/'SelectedFrame.png')
im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im);d.ellipse((3,3,124,124),outline='#78d0c7',width=3);im.save(R/'SelectionRing.png')
im=Image.new('RGB',(640,544),'#07131e');d=ImageDraw.Draw(im);rng=random.Random(84211)
for _ in range(70):
 x,y=rng.randrange(640),rng.randrange(544);d.point((x,y),fill=rng.choice(['#233647','#314757','#1c3142']))
for rx,ry in [(180,135),(280,220),(390,305),(500,390)]:d.ellipse((60-rx,300-ry,60+rx,300+ry),outline='#263d4e',width=1)
sun=Image.open(R/'Planet_Sol.png').resize((190,190));im.paste(sun,(-95,200),sun);im.save(R/'SystemChart.png')
print('Rendered original source-authored UI globe/selection/chart art.')

im=Image.new('RGBA',(48,48));d=ImageDraw.Draw(im);d.line((7,24,19,36,41,10),fill='#78d0c7',width=6);im.save(R/'Ready.png')
