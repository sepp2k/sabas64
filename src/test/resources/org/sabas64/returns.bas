10 gosub 100
20 gosub 200
30 gosub 300
40 gosub 400
50 gosub 500
55 on 23 gosub 300, 500
60 gosub 600
65 on 42 goto 500, 500
70 return
100 goto 600
200
300 return
400 goto 300
500 end
600
