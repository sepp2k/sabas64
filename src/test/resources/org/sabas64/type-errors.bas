10 print 1;tab(42);2;"hallo";spc(4);2.3
20 x = 42
30 y$ = "hallo"
40 z%= 42
45 x = z%
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
160 x = sin("la")
170 s$ = ~
180 rem array, type-correct
190 dim s$(12)
200 s$(4) = "hallo"
210 print s$(4)
220 dim a(10,10)
230 a(1,2) = 23
240 print a(1,2) + a(1,0)
250 rem array type errors
260 dim q("hallo"): q(1) = 42
270 print s$("hallo")
280 a(spc(4), "la") = 42
290 s$(1) = 42
300 a(1,2) = "hallo"
400 rem functions
410 def fn f(x) = x
420 def fn g(x,y) = x*y
430 print fn f(1)
440 print fn g(1,2)
450 rem functions type errors
460 def fn s$(x) = str$(x): rem string functions not allowed
470 def fn h(s$) = 42: rem string arguments not allowed
480 s$ = fn f(42)
490 x = fn f("hallo")
