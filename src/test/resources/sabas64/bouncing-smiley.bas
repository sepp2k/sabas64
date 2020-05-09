003 poke 53280, 6 : rem outer border = dark blue
006 poke 53281, 0 : rem background = black
010 gosub 500
020 v=53248 : rem VIC
030 poke v+21,12 : rem Enable sprite 2 and 3
035 poke v+41,7  : rem Smiley color = yellow
037 poke v+42,15 : rem Frowney color = grey
040 poke 2042,13 : rem Sprite 2 data from block 13
045 poke 2043,14 : rem Sprite 3 data from block 14
050 for i=0 to 62
060   read q
070   poke 832+i,q
080 next i
081 for i=0 to 62
082   read q
083   poke 896+i,q
084 next i
085 x = 25
090 y = 50
093 xd = 2
096 yd = 2
100 if x > 255 then poke v+4, x-255 : poke v+6, x-255 : poke v+16, 255
105 if x <= 255 then poke v+4, x : poke v+6, x : poke v+16, 0
110 poke v+5, y : poke v+7, 280-y
120 x = x + xd
130 y = y + yd
140 if x >= 320 or x <= 25 then xd = xd * -1
150 if y >= 131 or y <= 50 then yd = yd * -1
160 goto 100
500 print "{clear}"
510 poke 1024, 79: poke 55296, 1
520 for i=1 to 38
530   poke 1024+i, 119: poke 55296+i, 1
540 next i
550 poke 1063, 80: poke 55335, 1
560 for i=1 to 23
570   poke 1024 + 40*i, 116: poke 55296 + 40*i, 1
580   poke 1024 + 40*i+39, 106: poke 55296 + 40*i+39, 1
590 next i
600 poke 1984, 76: poke 56256, 1
610 for i=1 to 38
620   poke 1984+i, 111: poke 56256+i, 1
630 next i
640 poke 2023, 122: poke 56295, 1
650 return
1000 data 0, 0, 0
1001 data 0, 0, 0
1002 data 0, 255, 0
1003 data 3, 129, 128
1004 data 6, 0, 224
1005 data 12, 0, 48
1006 data 24, 0, 24
1007 data 48, 129, 8
1008 data 32, 0, 12
1009 data 32, 0, 4
1010 data 32, 0, 4
1011 data 32, 0, 4
1012 data 33, 0, 140
1013 data 49, 129, 136
1014 data 24, 195, 24
1015 data 12, 126, 48
1016 data 7, 0, 96
1017 data 1, 129, 192
1018 data 0, 255, 0
1019 data 0, 0, 0
1020 data 0, 0, 0
2000 data 0, 0, 0
2001 data 0, 0, 0
2002 data 0, 255, 0
2003 data 3, 129, 128
2004 data 6, 0, 224
2005 data 12, 0, 48
2006 data 24, 0, 24
2007 data 48, 129, 8
2008 data 32, 0, 12
2009 data 32, 0, 4
2010 data 32, 0, 4
2011 data 32, 0, 4
2012 data 32, 0, 12
2013 data 48, 126, 8
2014 data 24, 195, 24
2015 data 12, 129, 48
2016 data 6, 0, 96
2017 data 3, 129, 192
2018 data 0, 255, 0
2019 data 0, 0, 0
2020 data 0, 0, 0
