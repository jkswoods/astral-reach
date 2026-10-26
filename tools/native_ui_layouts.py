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

# Reconcile the observed UI1r2 render with the exact selected working concepts.
# Every replacement starts from the generator above, so reruns are idempotent.
p=r/'Astral.ui';s=p.read_text()
s=s.replace('@Text = (FontSize: 20','@Text = (FontSize: 24').replace('@Muted = (FontSize: 18','@Muted = (FontSize: 22').replace('@Heading = (FontSize: 21','@Heading = (FontName: "Secondary", FontSize: 24')
s=s.replace('(TexturePath: "../../Common/ContainerFullPatch.png", Border: 20)','(TexturePath: "Art/WindowPatch.png", Border: 12)').replace('(TexturePath: "../../Common/ContainerPatch.png", Border: 23)','(TexturePath: "Art/PanelPatch.png", Border: 8)')
s=s.replace('(TexturePath: "../../Common/ContainerHeaderNoRunes.png", HorizontalBorder: 35, VerticalBorder: 0)','(TexturePath: "Art/SectionHeader.png", Border: 8)')
s=s.replace('@Header = Group { Background: (TexturePath: "Art/SectionHeader.png", Border: 8)','@Header = Group { Background: (TexturePath: "Art/TitleHeader.png", Border: 8)')
for old,new in [('Tertiary','Row'),('Tertiary_Hovered','RowHovered'),('Tertiary_Pressed','RowPressed'),('Secondary','ControlSecondary'),('Secondary_Hovered','ControlSecondary_Hovered'),('Secondary_Pressed','ControlSecondary_Pressed')]:s=s.replace(f'(TexturePath: "../../Common/Buttons/{old}.png", Border: 12)',f'(TexturePath: "Art/{new}.png", Border: 8)')
s=s.replace('@ButtonLabel = (...$C.@DefaultButtonLabelStyle, FontSize: 20);','@ButtonLabel = (...$C.@DefaultButtonLabelStyle, FontName: "Secondary", FontSize: 24, RenderUppercase: false, MinShrinkTextToFitFontSize: 18);')
for old,new in [('Default','ControlPrimary'),('Hovered','ControlPrimary_Hovered'),('Pressed','ControlPrimary_Pressed'),('Disabled','ControlDisabled')]:s=s.replace('$C.@DefaultButton'+old+'Background',f'(TexturePath: "Art/{new}.png", Border: 8)')
s+='\n@Progress = ProgressBar { Background: "Art/MeterTrack.png"; BarTexturePath: "Art/MeterFill.png"; };\n';p.write_text(s)
for name in ['OrbitalMap.ui','LaunchYard.ui','Rocket.ui','Oxygen.ui','OxygenHud.ui','Ascent.ui','OrbitalMapHud.ui']:
 p=r/name;s=p.read_text().replace('$C.@ProgressBar','$A.@Progress').replace('(TexturePath: "../../Common/Buttons/Tertiary.png", Border: 12)','(TexturePath: "Art/Row.png", Border: 8)')
 if name=='OrbitalMap.ui':
  s=s.replace('$C.@SecondaryTextButton','$A.@Secondary').replace('$C.@TextButton','$A.@Primary')
  s=s.replace('FontSize: 36','FontName: "Secondary", FontSize: 38')
  s=s.replace('Label { Text: "DESTINATIONS";','Group { Anchor: (Left: 8, Width: 42, Height: 42); Background: "Art/Compass.png"; HitTestVisible: false; } Label { Anchor: (Left: 88, Right: 250); Text: "DESTINATIONS";')
  s=s.replace('Height: 260','Height: 258').replace('Height: 118','Height: 128').replace('Height: 146','Height: 156')
  s=s.replace('Group { Anchor: (Height: 12); }','Group { Anchor: (Height: 6); }')
  s=s.replace('Width: 170, Height: 170','Width: 190, Height: 190')
  mini=''.join(f'Group #Mini{n} {{ Anchor: (Left: 0, Top: 8, Width: 28, Height: 28); Background: "Art/Planet_{n}.png"; HitTestVisible: false; }} ' for n in planets)
  s=s.replace('Label #Selected { Text: "LUNE";',mini+'Label #Selected { Anchor: (Left: 40, Right: 0); Text: "LUNE";')
  s=s.replace('Group #PreviewOrbis','Group { Anchor: (Left: 8, Right: 8, Top: 48, Bottom: 8); Background: "Art/StarField.png"; HitTestVisible: false; }\nGroup #PreviewOrbis')
  s=s.replace('Top: 54, Height: 52','Top: 54, Height: 64').replace('Left: 4, Top: 2, Width: 48, Height: 48','Left: 4, Top: 4, Width: 56, Height: 56').replace('Left: 58, Right: 4, Top: 2, Height: 48','Left: 66, Right: 4, Top: 2, Height: 60').replace('FontSize: 18, TextColor: #d4e0e7','FontSize: 22, TextColor: #d4e0e7')
  s=s.replace('Top: 50, Height: 42','Top: 50, Height: 48').replace('Top: 96, Height: 42','Top: 104, Height: 48').replace('Left: 4, Width: 42, Height: 42','Left: 4, Width: 46, Height: 46').replace('Left: 58, Right: 40, Top: 2, Height: 38','Left: 62, Right: 44, Top: 2, Height: 44').replace('FontSize: 19, TextColor: #cbd9e1','FontSize: 22, TextColor: #cbd9e1')
  s=s.replace('Right: 10, Top: 9, Width: 24, Height: 24','Right: 9, Top: 9, Width: 30, Height: 30').replace('#e4c37b','#ffd46e')
  for n in ['Rustreach','Nacre','Vesper','Umbra']:
   start=s.index('Button #Route'+n);end=s.index('Group { Anchor: (Height: 9); }',start);row=s[start:end];row=row.replace('Right: 8, Height: 28','Right: 42, Height: 28').replace('Right: 8, Height: 26','Right: 42, Height: 26');row=row.rstrip();row=row[:-1]+f' Group {{ Anchor: (Right: 14, Top: 30, Width: 24, Height: 30); Background: "Art/Lock.png"; HitTestVisible: false; }} }} ';s=s[:start]+row+s[end:]
 elif name=='Oxygen.ui':
  s=s.replace('Width: 900, Height: 760','Width: 900, Height: 720').replace('Anchor: (Height: 220)','Anchor: (Height: 252)').replace('Left: 36, Right: 36, Top: 64, Bottom: 16','Width: 692, Top: 64, Bottom: 16').replace('Anchor: (Width: 180);','Anchor: (Width: 140);').replace('Anchor: (Height: 96);','Anchor: (Height: 124);').replace('Width: 84, Height: 84','Width: 104, Height: 104').replace('Height: 10, Left: 8','Height: 12, Left: 8').replace('FontSize: 21, TextColor: #b7dae3','FontSize: 24, TextColor: #b7dae3').replace('FontSize: 22, TextColor: #bddbe1','FontSize: 25, TextColor: #bddbe1')
  # Three true gutters; no invisible fourth gutter shifts the last canister.
  needle='Group { Anchor: (Width: 14); }';positions=[];pos=0
  while (pos:=s.find(needle,pos))>=0:positions.append(pos);pos+=len(needle)
  for i,pos in reversed(list(enumerate(positions))):s=s[:pos]+('Group { Anchor: (Width: 44); }' if i<3 else '')+s[pos+len(needle):]
 if name in ['LaunchYard.ui','Rocket.ui','Oxygen.ui']:
  s=s.replace('FontSize: 29','FontName: "Secondary", FontSize: 34').replace('FontSize: 27','FontName: "Secondary", FontSize: 34')
  for title in ['LAUNCH PLATFORM','OXYGEN DIFFUSER']:
   s=s.replace('Label { Text: "'+title+'";','Group { Anchor: (Left: 0, Width: 34, Height: 42); Background: "Art/RocketGlyph.png"; HitTestVisible: false; } Label { Anchor: (Left: 50, Right: 0); Text: "'+title+'";')
  if name=='Rocket.ui':s=s.replace('Label #RocketTitle {','Group { Anchor: (Left: 0, Width: 34, Height: 42); Background: "Art/RocketGlyph.png"; HitTestVisible: false; } Label #RocketTitle { Anchor: (Left: 50, Right: 0);').replace('Label { Text: "ROUND TRIP FUEL";','Label #FuelHeading { Text: "ROUND TRIP FUEL";')
 if name=='OxygenHud.ui':s=s.replace('FontSize: 17,','FontName: "Secondary", FontSize: 20,').replace('FontSize: 23,','FontSize: 25,')
 p.write_text(s)

p=r/'Oxygen.ui';s=p.read_text().replace('Art/RocketGlyph.png','Art/CanisterGlyph.png');p.write_text(s)
# Keep generated sources stable and clean after conditional gutters.
for name in ['Astral.ui','OrbitalMap.ui','LaunchYard.ui','Rocket.ui','Oxygen.ui','OxygenHud.ui','Ascent.ui','OrbitalMapHud.ui']:
 p=r/name;p.write_text('\n'.join(line.rstrip() for line in p.read_text().splitlines())+'\n')
