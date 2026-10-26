from pathlib import Path
r=Path(__file__).resolve().parents[1]/'src/main/resources/Common/UI/Custom/Pages/AstralReach'
(r/'Astral.ui').write_text('''$C = "../../Common.ui";
@Text = (FontSize: 20, TextColor: #cbd9e1, VerticalAlignment: Center);
@Muted = (FontSize: 18, TextColor: #9aafc2, VerticalAlignment: Center, Wrap: true);
@Heading = (FontSize: 21, TextColor: #b8ccdf, RenderBold: true, RenderUppercase: true, VerticalAlignment: Center, LetterSpacing: 1);
@Window = Group { Background: (TexturePath: "../../Common/ContainerFullPatch.png", Border: 20); };
@Header = Group { Background: (TexturePath: "../../Common/ContainerHeaderNoRunes.png", HorizontalBorder: 35, VerticalBorder: 0); Anchor: (Height: 62); Padding: (Horizontal: 24); };
@Section = Group { Background: (TexturePath: "../../Common/ContainerPatch.png", Border: 23); };
@SectionHeader = Group { Anchor: (Height: 42); Background: (TexturePath: "../../Common/ContainerHeaderNoRunes.png", HorizontalBorder: 35, VerticalBorder: 0); Padding: (Horizontal: 16); };
@Rule = Group { Anchor: (Height: 1); Background: #3c5266; };
@RowStyle = ButtonStyle(Default: (Background: (TexturePath: "../../Common/Buttons/Tertiary.png", Border: 12)), Hovered: (Background: (TexturePath: "../../Common/Buttons/Tertiary_Hovered.png", Border: 12)), Pressed: (Background: (TexturePath: "../../Common/Buttons/Tertiary_Pressed.png", Border: 12)), Sounds: $C.@ButtonSounds);
@PlanetStyle = ButtonStyle(Default: (Background: #00000000), Hovered: (Background: (TexturePath: "../../Common/Buttons/Tertiary_Hovered.png", Border: 12)), Pressed: (Background: (TexturePath: "../../Common/Buttons/Tertiary_Pressed.png", Border: 12)), Sounds: $C.@ButtonSounds);
''')
nav=['$C = "../../Common.ui";','$A = "Astral.ui";','$C.@PageOverlay { $A.@Window #Navigation { Anchor: (Width: 1440, Height: 870);',
'$A.@Header { Anchor: (Left: 8, Right: 8, Top: 8, Height: 62); Label { Text: "DESTINATIONS"; Style: (FontSize: 30, TextColor: #e4edf2, RenderBold: true, VerticalAlignment: Center, LetterSpacing: 1); } Label { Anchor: (Right: 0, Width: 230); Text: "ASTRAL REACH"; Style: (FontSize: 20, TextColor: #91abc4, HorizontalAlignment: End, VerticalAlignment: Center); } }',
'Group { Anchor: (Left: 20, Right: 20, Top: 82, Height: 680); LayoutMode: Left;',
'$A.@Section { Anchor: (Width: 280); $A.@SectionHeader { Anchor: (Left: 6, Right: 6, Top: 6, Height: 42); Label { Text: "ROUTES"; Style: $A.@Heading; } } Group { Anchor: (Left: 12, Right: 12, Top: 60, Bottom: 12); LayoutMode: Top;']
planets=['Orbis','Lune','Rustreach','Nacre','Vesper','Umbra']
for n in planets:
 nav.append(f'''Button #Route{n} {{ Anchor: (Height: 90); Style: $A.@RowStyle;
 Group #Chosen{n} {{ Anchor: (Full: 2); Background: (TexturePath: "Art/SelectedFrame.png", Border: 7); HitTestVisible: false; }}
 Group {{ Anchor: (Left: 8, Top: 11, Width: 66, Height: 66); Background: "Art/Planet_{n}.png"; HitTestVisible: false; }}
 Label {{ Anchor: (Left: 88, Top: 14, Right: 8, Height: 28); Text: "{n}"; Style: (FontSize: 23, TextColor: #e0e8ea, RenderBold: true); }}
 Label {{ Anchor: (Left: 88, Top: 48, Right: 8, Height: 26); Text: "{'Home' if n=='Orbis' else 'Available' if n=='Lune' else 'Future route'}"; Style: (FontSize: 17, TextColor: {'#78ccc8' if n=='Lune' else '#99adc1'}); }} }} Group {{ Anchor: (Height: 9); }}''')
nav+=['} } Group { Anchor: (Width: 14); }',
'$A.@Section { Anchor: (Width: 660); $A.@SectionHeader { Anchor: (Left: 6, Right: 6, Top: 6, Height: 42); Label { Text: "SYSTEM CHART"; Style: $A.@Heading; } }',
'Group { Anchor: (Left: 10, Right: 10, Top: 54, Bottom: 82); Background: "Art/SystemChart.png";']
coords={'Orbis':(200,245,76),'Lune':(320,260,54),'Rustreach':(385,115,68),'Nacre':(360,345,72),'Vesper':(200,450,74),'Umbra':(490,425,66)}
for n,(x,y,size) in coords.items():
 nav.append(f'''Button #Planet{n} {{ Anchor: (Left: {x}, Top: {y}, Width: {size+30}, Height: {size+32}); Style: $A.@PlanetStyle;
 Group #Ring{n} {{ Anchor: (Left: 0, Top: 0, Width: {size+6}, Height: {size+6}); Background: "Art/SelectionRing.png"; HitTestVisible: false; }}
 Group {{ Anchor: (Left: 3, Top: 3, Width: {size}, Height: {size}); Background: "Art/Planet_{n}.png"; HitTestVisible: false; }}
 Label {{ Anchor: (Left: 2, Top: {size+7}, Width: {size+30}, Height: 25); Text: "{n}"; Style: (FontSize: 19, TextColor: #d5dfe6); }} }}''')
nav+=['}', '$C.@SecondaryTextButton #Explore { Anchor: (Width: 260, Height: 46, Bottom: 18); Text: "Explore in 3D"; } }', 'Group { Anchor: (Width: 14); }',
'Group { Anchor: (Width: 428); LayoutMode: Top;',
'$A.@Section { Anchor: (Height: 260); $A.@SectionHeader { Anchor: (Left: 6, Right: 6, Top: 6, Height: 42); Label #Selected { Text: "LUNE"; Style: $A.@Heading; } }']
for n in planets:nav.append(f'Group #Preview{n} {{ Anchor: (Width: 170, Height: 170, Top: 50); Background: "Art/Planet_{n}.png"; }}')
nav+=['Label #Route { Anchor: (Left: 18, Right: 18, Bottom: 12, Height: 30); Style: $A.@Muted; } }','Group { Anchor: (Height: 12); }',
'$A.@Section { Anchor: (Height: 118); $A.@SectionHeader { Anchor: (Left: 6, Right: 6, Top: 6, Height: 38); Label { Text: "RESOURCES"; Style: $A.@Heading; } }',

'Group { Anchor: (Left: 16, Right: 16, Top: 54, Height: 52); LayoutMode: Left; Group { Anchor: (Width: 174); Background: (TexturePath: "../../Common/Buttons/Tertiary.png", Border: 12); ItemSlot #Resource0 { Anchor: (Left: 4, Top: 2, Width: 48, Height: 48); ShowQuantity: false; } Label #ResourceLabel0 { Anchor: (Left: 58, Right: 4, Top: 2, Height: 48); Style: (FontSize: 18, TextColor: #d4e0e7, VerticalAlignment: Center); } } Group { Anchor: (Width: 12); } Group { Anchor: (Width: 174); Background: (TexturePath: "../../Common/Buttons/Tertiary.png", Border: 12); ItemSlot #Resource1 { Anchor: (Left: 4, Top: 2, Width: 48, Height: 48); ShowQuantity: false; } Label #ResourceLabel1 { Anchor: (Left: 58, Right: 4, Top: 2, Height: 48); Style: (FontSize: 18, TextColor: #d4e0e7, VerticalAlignment: Center); } } } }',
'Group { Anchor: (Height: 12); }',
'$A.@Section { Anchor: (Height: 146); $A.@SectionHeader { Anchor: (Left: 6, Right: 6, Top: 6, Height: 38); Label { Text: "EXPEDITION"; Style: $A.@Heading; } }',

'Group { Anchor: (Left: 12, Right: 12, Top: 50, Height: 42); Background: (TexturePath: "../../Common/Buttons/Tertiary.png", Border: 12); ItemSlot #SuitIcon { Anchor: (Left: 4, Width: 42, Height: 42); ItemId: "AR_Survey_Chest"; ShowQuantity: false; } Label #Suit { Anchor: (Left: 58, Right: 40, Top: 2, Height: 38); Style: (FontSize: 19, TextColor: #cbd9e1, VerticalAlignment: Center, Wrap: true); } Group #SuitCheck { Anchor: (Right: 10, Top: 9, Width: 24, Height: 24); Background: "Art/Ready.png"; } }',
'Group { Anchor: (Left: 12, Right: 12, Top: 96, Height: 42); Background: (TexturePath: "../../Common/Buttons/Tertiary.png", Border: 12); ItemSlot { Anchor: (Left: 4, Width: 42, Height: 42); ItemId: "AR_OxygenCanister"; ShowQuantity: false; } Label #Oxygen { Anchor: (Left: 58, Right: 40, Top: 2, Height: 38); Style: (FontSize: 19, TextColor: #cbd9e1, VerticalAlignment: Center, Wrap: true); } Group #OxygenCheck { Anchor: (Right: 10, Top: 9, Width: 24, Height: 24); Background: "Art/Ready.png"; } } }',
'Group { Anchor: (Height: 12); }',
'$A.@Section { Anchor: (Height: 120); $A.@SectionHeader { Anchor: (Left: 6, Right: 6, Top: 6, Height: 38); Label { Text: "RETURN RESERVE"; Style: $A.@Heading; } }',
'ItemSlot { Anchor: (Left: 16, Top: 52, Width: 44, Height: 44); ItemId: "AR_PropellantCell"; ShowQuantity: false; } Label #Reserve { Anchor: (Left: 74, Top: 50, Right: 14, Height: 32); Style: (FontSize: 21, TextColor: #e4c37b, RenderBold: true); } Label #Detail { Anchor: (Left: 18, Right: 18, Bottom: 8, Height: 28); Style: (FontSize: 17, TextColor: #9aafc2, Wrap: true); } }',
'} }',
'$A.@Rule { Anchor: (Left: 20, Right: 20, Bottom: 86, Height: 1); }',
'$C.@SecondaryTextButton #Close { Anchor: (Left: 28, Bottom: 24, Width: 248, Height: 46); Text: "Close"; } Label #Flight { Anchor: (Left: 308, Bottom: 24, Width: 640, Height: 46); Style: (FontSize: 19, TextColor: #a5b8c8, HorizontalAlignment: Center, VerticalAlignment: Center); }',
'$C.@TextButton #Travel { Anchor: (Right: 28, Bottom: 24, Width: 400, Height: 46); Text: "Land on Lune"; }',
'} }']
(r/'OrbitalMap.ui').write_text('\n'.join(nav)+'\n')
# All controls use a single native style at readable playing scale.
p=r/'Astral.ui';s=p.read_text();s+='''
@ButtonLabel = (...$C.@DefaultButtonLabelStyle, FontSize: 20);
@DisabledLabel = (...@ButtonLabel, TextColor: #7f8c99);
@Primary = TextButton { Anchor: (Height: 46); Style: (Default: (Background: $C.@DefaultButtonDefaultBackground, LabelStyle: @ButtonLabel), Hovered: (Background: $C.@DefaultButtonHoveredBackground, LabelStyle: @ButtonLabel), Pressed: (Background: $C.@DefaultButtonPressedBackground, LabelStyle: @ButtonLabel), Disabled: (Background: $C.@DefaultButtonDisabledBackground, LabelStyle: @DisabledLabel), Sounds: $C.@ButtonSounds); };
@Secondary = TextButton { Anchor: (Height: 46); Style: (Default: (Background: (TexturePath: "../../Common/Buttons/Secondary.png", Border: 12), LabelStyle: @ButtonLabel), Hovered: (Background: (TexturePath: "../../Common/Buttons/Secondary_Hovered.png", Border: 12), LabelStyle: @ButtonLabel), Pressed: (Background: (TexturePath: "../../Common/Buttons/Secondary_Pressed.png", Border: 12), LabelStyle: @ButtonLabel), Disabled: (Background: $C.@DefaultButtonDisabledBackground, LabelStyle: @DisabledLabel), Sounds: $C.@ButtonSounds); };
''';p.write_text(s)
p=r/'OrbitalMap.ui';s=p.read_text().replace('Anchor: (Full: 2)','Anchor: (Left: 2, Right: 2, Top: 2, Bottom: 2)').replace('Width: 280','Width: 320').replace('Width: 428','Width: 392').replace('Width: 142','Width: 126').replace('FontSize: 30','FontSize: 36');p.write_text(s)
# The yard keeps native controls and real server item counts, with a compact completed state.
yard='''$C = "../../Common.ui";
$A = "Astral.ui";
$C.@PageOverlay { $A.@Window #YardPanel { Anchor: (Width: 1000, Height: 780);
 $A.@Header { Anchor: (Left: 8, Right: 8, Top: 8, Height: 56); Label { Text: "LAUNCH PLATFORM"; Style: (FontSize: 29, TextColor: #e0e9ee, RenderBold: true, VerticalAlignment: Center); } }
 Group { Anchor: (Left: 26, Right: 26, Top: 82, Bottom: 92); LayoutMode: Top;
  Group #BlueprintSummary { Anchor: (Height: 170); LayoutMode: Left;
   $A.@Section { Anchor: (Width: 220); ItemSlot #ShipIcon { Anchor: (Width: 150, Height: 150); ShowQuantity: false; } }
   Group { Anchor: (Width: 22); }
   Group { FlexWeight: 1; LayoutMode: Top;
    Label #Status { Anchor: (Height: 45); Style: (FontSize: 24, TextColor: #d4e6e4, RenderBold: true, Wrap: true); }
    Label #Footprint { Anchor: (Height: 34); Style: $A.@Muted; }
    $A.@Rule { }
    Label #Blueprint { Anchor: (Height: 64); Style: (FontSize: 23, TextColor: #a8cfda, Wrap: true, VerticalAlignment: Center); }
   }
  }
  Group #Plans { Anchor: (Height: 72); LayoutMode: Left;
   $A.@Primary #Odyssey { Anchor: (Width: 300, Height: 52); Text: "Odyssey Heavy"; }
   Group { Anchor: (Width: 14); }
   $A.@Secondary #Kestrel { Anchor: (Width: 300, Height: 52); Text: "Kestrel Stack"; }
   Group { Anchor: (Width: 14); }
   $A.@Secondary #Rotate { Anchor: (Width: 300, Height: 52); Text: "Pad: North"; }
  }
  $A.@Section #Materials { Anchor: (Height: 380);
   $A.@SectionHeader { Anchor: (Left: 6, Right: 6, Top: 6, Height: 42); Label { Text: "PARTS STILL NEEDED"; Style: $A.@Heading; } }
   Group { Anchor: (Left: 22, Right: 22, Top: 56, Height: 34); LayoutMode: Left; Label { Anchor: (Width: 532); Text: "Part"; Style: $A.@Muted; } Label { Anchor: (Width: 150); Text: "Needed"; Style: (FontSize: 18, TextColor: #99afc2, HorizontalAlignment: Center); } Label { Anchor: (Width: 150); Text: "In pack"; Style: (FontSize: 18, TextColor: #99afc2, HorizontalAlignment: Center); } }
   Group { Anchor: (Left: 22, Right: 22, Top: 92, Bottom: 10); LayoutMode: Top;
'''
for i in range(5):yard+=f'''Group #Material{i} {{ Anchor: (Height: 52); LayoutMode: Top;
 Group {{ Anchor: (Height: 48); LayoutMode: Left;
  ItemSlot #Icon {{ Anchor: (Width: 48, Height: 48); ShowQuantity: false; }} Group {{ Anchor: (Width: 14); }}
  Label #Count {{ Anchor: (Width: 470); Style: $A.@Text; }} Label #Needed {{ Anchor: (Width: 150); Style: (FontSize: 22, TextColor: #c7e1e8, HorizontalAlignment: Center, VerticalAlignment: Center); }} Label #Carried {{ Anchor: (Width: 150); Style: (FontSize: 22, TextColor: #c7e1e8, HorizontalAlignment: Center, VerticalAlignment: Center); }}
 }} $A.@Rule {{ }} }}\n'''
yard+='''   }
  }
  Group #Work { Anchor: (Height: 78); LayoutMode: Top; Label #WorkText { Anchor: (Height: 32); Style: $A.@Text; } $C.@ProgressBar #WorkProgress { Anchor: (Height: 16); Value: 0; } }
  Label #Recovery { Anchor: (Height: 36); Style: (FontSize: 19, TextColor: #d9bb79, Wrap: true); }
  Label #Help { Anchor: (Height: 48); Style: $A.@Muted; Text: "Only missing parts are supplied. Paid work pauses if the pad or clearance is blocked."; }
 }
 $A.@Rule { Anchor: (Left: 24, Right: 24, Bottom: 82, Height: 1); }
 $A.@Secondary #Close { Anchor: (Left: 28, Bottom: 24, Width: 168, Height: 46); Text: "Close"; }
 $A.@Secondary #Cancel { Anchor: (Left: 210, Bottom: 24, Width: 340, Height: 46); Text: "Return supplied parts"; }
 $A.@Primary #Deposit { Anchor: (Right: 28, Bottom: 24, Width: 390, Height: 46); Text: "Supply needed parts"; }
 $A.@Primary #Build { Anchor: (Right: 28, Bottom: 24, Width: 390, Height: 46); Text: "Retry construction"; }
 $A.@Primary #Flight { Anchor: (Right: 28, Bottom: 24, Width: 390, Height: 46); Text: "Open flight control"; }
} }
''';(r/'LaunchYard.ui').write_text(yard)
(r/'Rocket.ui').write_text('''$C = "../../Common.ui";
$A = "Astral.ui";
$C.@PageOverlay { $A.@Window #FlightPanel { Anchor: (Width: 900, Height: 720);
 $A.@Header { Anchor: (Left: 8, Right: 8, Top: 8, Height: 56); Label #RocketTitle { Text: "FLIGHT CONTROL"; Style: (FontSize: 27, TextColor: #e3ebf0, RenderBold: true, VerticalAlignment: Center); } }
 Group { Anchor: (Left: 26, Right: 26, Top: 80, Bottom: 92); LayoutMode: Top;
  Label #Status { Anchor: (Height: 44); Style: (FontSize: 23, TextColor: #a3d7d1, RenderBold: true, Wrap: true); }
  $A.@Section #Fuel { Anchor: (Height: 220);
   $A.@SectionHeader { Anchor: (Left: 6, Right: 6, Top: 6, Height: 42); Label { Text: "ROUND TRIP FUEL"; Style: $A.@Heading; } }
   ItemSlot { Anchor: (Left: 22, Top: 68, Width: 58, Height: 58); ItemId: "AR_PropellantCell"; ShowQuantity: false; }
   $C.@ProgressBar #FuelProgress { Anchor: (Left: 100, Right: 22, Top: 64, Height: 20); Value: 0; }
   Label #FuelCount { Anchor: (Left: 100, Right: 22, Top: 92, Height: 38); Style: (FontSize: 24, TextColor: #c2e2e7, RenderBold: true); }
   Label #Route { Anchor: (Left: 24, Right: 310, Top: 144, Height: 64); Style: $A.@Muted; }
   $A.@Secondary #Drain { Anchor: (Right: 22, Top: 156, Width: 270, Height: 44); Text: "Recover one cell"; }
  }
  Group { Anchor: (Height: 16); }
  $A.@Section #Preparation { Anchor: (Height: 186);
   $A.@SectionHeader { Anchor: (Left: 6, Right: 6, Top: 6, Height: 42); Label { Text: "PREPARATION"; Style: $A.@Heading; } }
   ItemSlot { Anchor: (Left: 22, Top: 58, Width: 48, Height: 48); ItemId: "AR_Survey_Chest"; ShowQuantity: false; }
   Label #Suit { Anchor: (Left: 90, Right: 22, Top: 62, Height: 38); Style: $A.@Text; }
   $A.@Rule { Anchor: (Left: 22, Right: 22, Top: 118, Height: 1); }
   ItemSlot { Anchor: (Left: 22, Top: 130, Width: 48, Height: 48); ItemId: "AR_OxygenCanister"; ShowQuantity: false; }
   Label #Oxygen { Anchor: (Left: 90, Right: 22, Top: 134, Height: 38); Style: $A.@Text; }
  }
  Label #Help { Anchor: (Height: 54); Style: $A.@Muted; Text: "Board the rocket and ascend to orbit."; }
 }
 $A.@Rule { Anchor: (Left: 24, Right: 24, Bottom: 82, Height: 1); }
 $A.@Secondary #Close { Anchor: (Left: 28, Bottom: 24, Width: 180, Height: 46); Text: "Close"; }
 $A.@Primary #Fill { Anchor: (Right: 28, Bottom: 24, Width: 420, Height: 46); Text: "Load trip fuel"; }
 $A.@Primary #Rebuild { Anchor: (Right: 28, Bottom: 24, Width: 420, Height: 46); Text: "Supply missing parts"; }
 $A.@Primary #Launch { Anchor: (Right: 28, Bottom: 24, Width: 420, Height: 46); Text: "Launch to orbit"; }
} }
''')
oxy='''$C = "../../Common.ui";
$A = "Astral.ui";
$C.@PageOverlay { $A.@Window { Anchor: (Width: 900, Height: 760);
 $A.@Header { Anchor: (Left: 8, Right: 8, Top: 8, Height: 56); Label { Text: "OXYGEN DIFFUSER"; Style: (FontSize: 29, TextColor: #e2eaf0, RenderBold: true, VerticalAlignment: Center); } }
 Group { Anchor: (Left: 26, Right: 26, Top: 80, Bottom: 92); LayoutMode: Top;
  Label #Status { Anchor: (Height: 46); Style: (FontSize: 24, TextColor: #9ed7d1, RenderBold: true); }
  $A.@Section { Anchor: (Height: 220);
   $A.@SectionHeader { Anchor: (Left: 6, Right: 6, Top: 6, Height: 42); Label #CanisterCount { Text: "CANISTERS"; Style: $A.@Heading; } }
   Group { Anchor: (Left: 36, Right: 36, Top: 64, Bottom: 16); LayoutMode: Left;
'''
for i in range(4):
 oxy+=f'''Group {{ Anchor: (Width: 180); LayoutMode: Top;
 Group {{ Anchor: (Height: 96); Background: (TexturePath: "../../Common/Buttons/Tertiary.png", Border: 12); ItemSlot #Canister{i} {{ Anchor: (Width: 84, Height: 84); ShowQuantity: false; }} }}
 $C.@ProgressBar #Charge{i} {{ Anchor: (Height: 10, Left: 8, Right: 8); Value: 0; }} Label #ChargeText{i} {{ Anchor: (Height: 34); Style: (FontSize: 21, TextColor: #b7dae3, HorizontalAlignment: Center, VerticalAlignment: Center); }} }} Group {{ Anchor: (Width: 14); }}\n'''
oxy+='''   }
  }
  Group { Anchor: (Height: 16); }
  $A.@Section { Anchor: (Height: 160);
   $A.@SectionHeader { Anchor: (Left: 6, Right: 6, Top: 6, Height: 42); Label { Text: "FIELD"; Style: $A.@Heading; } }
   ItemSlot { Anchor: (Left: 26, Top: 66, Width: 72, Height: 72); ItemId: "AR_OxygenDiffuser"; ShowQuantity: false; }
   Label #Field { Anchor: (Left: 122, Right: 22, Top: 58, Height: 92); Style: (FontSize: 22, TextColor: #bddbe1, Wrap: true); }
  }
  Group { Anchor: (Height: 18); }
  Group { Anchor: (Height: 50); LayoutMode: Left; $A.@Secondary #Load { Anchor: (Width: 412, Height: 46); Text: "Load one canister"; } Group { Anchor: (Width: 16); } $A.@Secondary #Collect { Anchor: (Width: 412, Height: 46); Text: "Collect all"; } }
 }
 $A.@Rule { Anchor: (Left: 24, Right: 24, Bottom: 82, Height: 1); }
 $A.@Secondary #Close { Anchor: (Left: 28, Bottom: 24, Width: 180, Height: 46); Text: "Close"; }
 $A.@Primary #Toggle { Anchor: (Right: 28, Bottom: 24, Width: 330, Height: 46); Text: "Switch on"; }
} }
''';(r/'Oxygen.ui').write_text(oxy)
(r/'OxygenHud.ui').write_text('''$C = "../../Common.ui";
$A = "Astral.ui";
Group { HitTestVisible: false;
 $A.@Section { Anchor: (Right: 28, Bottom: 148, Width: 350, Height: 142);
  $A.@SectionHeader { Anchor: (Left: 5, Right: 5, Top: 5, Height: 32); Label { Text: "OXYGEN"; Style: (FontSize: 17, TextColor: #b4cfdf, RenderBold: true, VerticalAlignment: Center); } }
  ItemSlot { Anchor: (Left: 16, Top: 48, Width: 42, Height: 54); ItemId: "AR_OxygenCanister"; ShowQuantity: false; }
  $C.@ProgressBar #OxygenFill { Anchor: (Left: 76, Right: 18, Top: 48, Height: 12); Value: 0; }
  Label #Oxygen { Anchor: (Left: 76, Right: 16, Top: 70, Height: 30); Style: (FontSize: 23, TextColor: #c8e7df, RenderBold: true); }
  Label #Environment { Anchor: (Left: 16, Right: 16, Bottom: 10, Height: 30); Style: (FontSize: 15, TextColor: #a4bdcb, Wrap: true); }
 }
}
''')
(r/'Ascent.ui').write_text('''$C = "../../Common.ui";
$A = "Astral.ui";
Group {
 $A.@Section { Anchor: (Top: 30, Left: 40, Width: 540, Height: 152);
  $A.@SectionHeader { Anchor: (Left: 5, Right: 5, Top: 5, Height: 34); Label { Text: "ASTRAL REACH · FLIGHT"; Style: (FontSize: 18, TextColor: #b4cddd, RenderBold: true, VerticalAlignment: Center); } }
  Label #Stage { Anchor: (Left: 20, Right: 20, Top: 50, Height: 56); Text: "IGNITION"; Style: (FontSize: 26, TextColor: #e4edf1, Wrap: true, RenderBold: true); }
  Group #Progress { Anchor: (Left: 20, Right: 20, Bottom: 18, Height: 18); $C.@ProgressBar #FlightProgress { Anchor: (Left: 0, Right: 76, Height: 14); Value: 0; } Label #FlightPercent { Anchor: (Right: 0, Width: 62, Height: 22); Style: (FontSize: 19, TextColor: #b1d5dc, HorizontalAlignment: End); } }
 }
 $A.@Secondary #Abort { Anchor: (Bottom: 30, Left: 40, Width: 300, Height: 44); Text: "Abort launch"; }
}
''')
(r/'OrbitalMapHud.ui').write_text('''$A = "Astral.ui";
Group { HitTestVisible: false;
 $A.@Section { Anchor: (Left: 30, Top: 28, Width: 580, Height: 88); Padding: (Full: 16); LayoutMode: Top;
  Label { Anchor: (Height: 28); Text: "SYSTEM CHART · 3D VIEW"; Style: (FontSize: 24, TextColor: #d5e4e9, RenderBold: true); }
  Label { Anchor: (Height: 24); Text: "Drag to pan · Scroll to zoom · Click a planet"; Style: (FontSize: 17, TextColor: #a9c4d3); }
 }
 $A.@Section { Anchor: (Left: 30, Bottom: 28, Width: 840, Height: 52); Padding: (Full: 14); Label { Text: "Right-click: destinations · Middle-click: reset view"; Style: (FontSize: 19, TextColor: #bdd3df); } }
}
''')
for p in r.glob('*.ui'):
 if p.name not in ['Astral.ui','YardSlot.ui','YardRow.ui']:
  s=p.read_text().replace('$C.@TextButton','$A.@Primary').replace('$C.@SecondaryTextButton','$A.@Secondary')
  p.write_text(s)
