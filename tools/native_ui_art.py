#!/usr/bin/env python3
"""Original source-authored native UI decoration; real controls remain native widgets."""
from pathlib import Path
import math,random
from PIL import Image,ImageDraw,ImageFilter
R=Path(__file__).resolve().parents[1]/'src/main/resources/Common/UI/Custom/Pages/AstralReach/Art';R.mkdir(parents=True,exist_ok=True)
PALETTES={'Sol':((238,182,65),(255,231,132)),'Orbis':((48,139,193),(107,160,73)),'Lune':((181,180,177),(143,145,151)),'Rustreach':((196,99,56),(122,64,41)),'Nacre':((149,215,226),(196,229,231)),'Vesper':((213,175,68),(148,109,45)),'Umbra':((144,107,184),(184,152,216))}
# Painted voxel globes. The previous constant per-axis lighting made the near
# hemisphere read as a terraced disc. Light both the local face and the whole
# sphere; vary the radial surface so latitude steps do not join into bands.
for name,(a,b) in PALETTES.items():
 size=256;scale=3;im=Image.new('RGBA',(size*scale,size*scale));d=ImageDraw.Draw(im);faces=[];radius=9 if name=='Lune' else 12;unit=12.0 if name=='Lune' else 9.0
 def relief(x,y,z):
  length=math.sqrt(x*x+y*y+z*z)
  if length==0:return radius
  nx,ny,nz=x/length,y/length,z/length
  height=.23*math.sin(nx*16+ny*9)*math.cos(nz*11-ny*5)+.17*math.sin(ny*21+nz*13+nx*4)
  if name=='Lune':
   # Radial bowls with raised rims, rather than holes cut into a flat front.
   for cx,cy,cz,r,depth in [(-.40,.31,.86,.255,2.25),(.31,-.22,.925,.22,1.8),(-.80,-.30,.52,.20,1.4),(.26,.76,.60,.15,1.15),(.68,.30,.67,.14,1.2),(-.13,-.72,.68,.135,1.1)]:
    cl=math.sqrt(cx*cx+cy*cy+cz*cz);dist=math.sqrt((nx-cx/cl)**2+(ny-cy/cl)**2+(nz-cz/cl)**2)
    height-=depth*max(0,1-(dist/r)**2)**1.2
    height+=.65*math.exp(-((dist-r)/(r*.18))**2)
  elif name=='Rustreach':height+=.40*math.sin(nx*12+nz*8)*math.sin(ny*10-nz*4)
  elif name=='Nacre':height+=.25*math.sin(nx*17+ny*20+nz*13)
  return radius+height
 voxels={(x,y,z) for x in range(-13,14) for y in range(-13,14) for z in range(-13,14) if x*x+y*y+z*z<relief(x,y,z)**2}
 yaw=.62;pitch=.38
 def project(p):
  x,y,z=p;xx=x*math.cos(yaw)+z*math.sin(yaw);zz=-x*math.sin(yaw)+z*math.cos(yaw);yy=y*math.cos(pitch)-zz*math.sin(pitch);depth=y*math.sin(pitch)+zz*math.cos(pitch)
  return ((size/2+xx*unit)*scale,(size/2-yy*unit)*scale,depth)
 data=[((-1,0,0),[(-.5,-.5,-.5),(-.5,.5,-.5),(-.5,.5,.5),(-.5,-.5,.5)]),((0,1,0),[(-.5,.5,-.5),(.5,.5,-.5),(.5,.5,.5),(-.5,.5,.5)]),((0,0,1),[(-.5,-.5,.5),(.5,-.5,.5),(.5,.5,.5),(-.5,.5,.5)])]
 light=(-.63,.70,.66);ll=math.sqrt(sum(c*c for c in light));light=tuple(c/ll for c in light)
 for x,y,z in voxels:
  length=max(1,math.sqrt(x*x+y*y+z*z));normal=(x/length,y/length,z/length)
  for (dx,dy,dz),corners in data:
   if (x+dx,y+dy,z+dz) in voxels:continue
   p=[project((x+cx,y+cy,z+cz)) for cx,cy,cz in corners]
   nx,ny,nz=normal;f=math.sin(nx*6+nz*3)+math.cos(ny*7-nz*5)+.45*math.sin(nx*11+ny*6+nz*4)
   mix=max(0,min(1,(f-.5)*1.8));color=tuple(a[i]*(1-mix)+b[i]*mix for i in range(3))
   if name=='Orbis' and f>.65:color=(99,151,69)
   if name=='Orbis' and (math.sin(nx*12+ny*7)+math.cos(nz*9-ny*14)>1.5):color=(189,208,200)
   sphere=max(0,sum(normal[i]*light[i] for i in range(3)));face=max(0,dx*light[0]+dy*light[1]+dz*light[2])
   # Broad material variation and a coherent dark limb remain readable at 40px.
   brush=.94+.075*math.sin(x*1.7+y*.8+z*2.4)+.045*math.sin(x*4.1-y*2.8+z*.7)
   shade=(.38+.78*sphere)*(.75+.35*face)*brush
   if name=='Lune':
    # Face-to-light visibility makes the crater walls cast actual broad
    # shadows. Larger voxels preserve the selected concept's block relief
    # when this original icon is reduced to its in-game size.
    origin=(x+dx*.55,y+dy*.55,z+dz*.55)
    occluded=any(tuple(round(origin[i]+light[i]*t*.65) for i in range(3)) in voxels for t in range(2,15))
    if occluded:shade*=.59
    for cx,cy,cz,r in [(-.40,.31,.86,.255),(.31,-.22,.925,.22),(-.80,-.30,.52,.20),(.26,.76,.60,.15),(.68,.30,.67,.14),(-.13,-.72,.68,.135)]:
     cl=math.sqrt(cx*cx+cy*cy+cz*cz);distance=math.sqrt(sum((normal[i]-v/cl)**2 for i,v in enumerate((cx,cy,cz))))/r
     if distance<.86:shade*=.46+.35*distance
     elif distance<1.18:shade*=1.12
   if name=='Sol':shade=.91+.15*sphere+.04*math.sin(x+y+z)
   color=tuple(min(255,max(0,int(c*shade))) for c in color)+(255,)
   faces.append((sum(q[2] for q in p)/4,[(q[0],q[1]) for q in p],color))
 for _,p,c in sorted(faces,key=lambda q:q[0]):d.polygon(p,fill=c)
 im.resize((size,size),Image.Resampling.LANCZOS).save(R/f'Planet_{name}.png')

def panel(name,top,bottom,edge='#476780',cut=3):
 w,h=96,64;im=Image.new('RGBA',(w,h));d=ImageDraw.Draw(im);t=tuple(bytes.fromhex(top[1:]));b=tuple(bytes.fromhex(bottom[1:]));poly=[(cut,0),(w-cut-1,0),(w-1,cut),(w-1,h-cut-1),(w-cut-1,h-1),(cut,h-1),(0,h-cut-1),(0,cut)]
 for y in range(h):
  c=tuple(round(t[i]+(b[i]-t[i])*y/(h-1)) for i in range(3));d.line((0,y,w,y),fill=c)
 mask=Image.new('L',(w,h));ImageDraw.Draw(mask).polygon(poly,fill=255);im.putalpha(mask);d=ImageDraw.Draw(im)
 d.line(poly+[poly[0]],fill='#080f18',width=1)
 d.line([(cut+1,1),(w-cut-2,1),(w-2,cut+1),(w-2,h-cut-2),(w-cut-2,h-2),(cut+1,h-2),(1,h-cut-2),(1,cut+1),(cut+1,1)],fill=edge,width=1)
 if name in ('WindowPatch.png','PanelPatch.png'):
  # A restrained angular corner etching replaces the repeated thick bevel.
  for flipx,flipy in [(False,False),(True,False),(False,True),(True,True)]:
   pts=[(4,10),(4,4),(10,4),(7,7),(4,4)]
   pts=[(w-1-x if flipx else x,h-1-y if flipy else y) for x,y in pts];d.line(pts,fill='#284b67',width=1)
 if name=='SectionHeader.png':
  # A section heading is a strip inside its enclosing frame, not another box.
  d=ImageDraw.Draw(im);d.rectangle((0,0,w-1,h-1),outline=top,width=3);d.line((0,h-1,w-1,h-1),fill=edge,width=1)
 im.save(R/name)
for args in [('WindowPatch.png','#102236','#091522','#54758e'),('PanelPatch.png','#0e2030','#091722','#527d9a'),('TitleHeader.png','#1d3c60','#102a46','#6d97bc'),('SectionHeader.png','#193b59','#10283c','#507e9f'),('Row.png','#112333','#0b1b29','#3e6682'),('RowHovered.png','#17323e','#0c2734','#75bcc6'),('RowPressed.png','#10232d','#0b1b24','#5fadb8'),('ControlPrimary.png','#126be3','#084ba6','#74b3f4'),('ControlPrimary_Hovered.png','#258bff','#1261c4','#b1e6ff'),('ControlPrimary_Pressed.png','#0b57ae','#083b7c','#5889a6'),('ControlSecondary.png','#354353','#253243','#7c90a3'),('ControlSecondary_Hovered.png','#45596d','#304457','#a3b8c9'),('ControlSecondary_Pressed.png','#25364b','#172738','#4b7895'),('ControlDisabled.png','#2b3743','#202b36','#475869')]:panel(*args)
im=Image.new('RGBA',(64,64),(0,170,184,38));d=ImageDraw.Draw(im);d.rectangle((1,1,62,62),outline='#16ffed',width=4);im.save(R/'SelectedFrame.png')
im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im);d.ellipse((3,3,124,124),outline='#78e4da',width=3);im.save(R/'SelectionRing.png')
for name,alpha in [('PlanetHover.png',45),('PlanetPressed.png',80)]:
 im=Image.new('RGBA',(192,224));d=ImageDraw.Draw(im);d.ellipse((9,9,183,183),fill=(66,166,183,alpha),outline=(105,224,222,130),width=2);im.save(R/name)
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
im=Image.new('RGB',(640,614),'#07131e');d=ImageDraw.Draw(im);rng=random.Random(84211)
for _ in range(100):
 x,y=rng.randrange(640),rng.randrange(614);d.rectangle((x,y,x+1,y+1),fill=rng.choice(['#334b60','#61778c','#294257']))
for rx,ry in [(180,135),(280,220),(390,305),(500,390)]:d.ellipse((60-rx,335-ry,60+rx,335+ry),outline='#314c61',width=1)
# A sparse original belt of painted fragments follows the system chart's inner orbital arc.
for i in range(42):
 t=-1.3+i*.063;x=60+217*math.cos(t)+rng.uniform(-7,7);y=335+168*math.sin(t)+rng.uniform(-6,6);size=rng.choice([2,3,4])
 d.polygon([(x-size,y),(x-size*.6,y-size*.75),(x+size*.5,y-size),(x+size,y),(x+size*.5,y+size*.7),(x-size*.3,y+size)],fill=rng.choice(['#626267','#797a7c','#4c505a']))
 d.line((x-size*.6,y-size*.7,x+size*.4,y-size*.8),fill='#a1a1a3',width=1)
glow=Image.new('RGBA',im.size);ImageDraw.Draw(glow).ellipse((-147,183,147,477),fill=(207,151,43,85));glow=glow.filter(ImageFilter.GaussianBlur(25));im=Image.alpha_composite(im.convert('RGBA'),glow)
sun=Image.open(R/'Planet_Sol.png').resize((280,280));im.paste(sun,(-140,190),sun);im.convert('RGB').save(R/'SystemChart.png')
print('Rendered original voxel relief, native nine-slice frames, glyphs and teal meter art; loaded comparison pending.')
im=Image.new('RGB',(640,400),'#091622');d=ImageDraw.Draw(im);rng=random.Random(9440)
for _ in range(110):
 x,y=rng.randrange(640),rng.randrange(400);d.rectangle((x,y,x+1,y+1),fill=rng.choice(['#456078','#758c9e','#29465b']))
im.save(R/'StarField.png')
