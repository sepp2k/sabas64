10 print 1;tab(42);2;"hallo";spc(4);2.3
20 x = 42
30 y$ = "hallo"
40 z%= 42
50 y$ = mid$(y$, 2, 3)
60 if y$ then print "y$ is not empty"
70 if x then print "x is not zero"
80 rem and now the errors
90 x = spc(4)
100 x = "hallo"
110 y$ = 42
120 y$ = spc(4)
130 if tab(42) then print "Invalid!"
140 y$ = mid$(2, 3, y$)
150 y$ = mid$(y$, 2)
