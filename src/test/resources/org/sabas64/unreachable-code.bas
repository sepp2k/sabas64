10 goto 30 : print "not reachable 1"
20 print "not reachable 2"
30 print "reachable"
40 gosub 100
50 end
100 print "also reachable"
110 return
120 print "not reachable 3"
130 goto 120
