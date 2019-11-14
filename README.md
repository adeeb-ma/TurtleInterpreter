# TurtleInterpreter

## Code instructions
* fd / forward *num* -------------- move cursor forward by *num* pixels.
* bk / back / backward *num* ------ move cursor backwards by *num* pixels.
* rt / right *num* ---------------- rotate cursor right by *num* degrees.
* lf / left *num* ----------------- rotate cursor left by *num* degrees.
* make "*var* *num* --------------- create a global variable by the name of *var* with the value of *num*. **Note the " at the start of the *var*!**. To access the variable, simply type :*var* (the : is part of the syntax).
* repeat *num* [*code*] ----------- do the *logic code* in the "[]", and keep doing it *num* times.
* stop ---------------------------- stop the execution **within the current stack**.
* if *expression* [*code*] -------- an if statement, having the *expression* being true, then execute the code in the block.
* if *expression* if_true [*code1*] if_false [*code2*] --- a more flexible if statement. If the *expression* is true, then execute *code1* only. If the expression is false, then execute *code2*.

## Function Declaration
To declare a custom function, type 'decl' in the interpreter and press Enter.
Follow the format as if you're writing in a regular text editor:
```
func *function_name* :*param1* :*param2 ...
*code*
end
```
After finishing the code, press **Esc** to go back to the regular interpreter. To call the function, simply type its name and hit Enter (with its parameters too).

## Example Codes

### Fractal Tree (Fucntion):
```
func tree :n :len
if :n<1 [stop]
fd :n*:len
rt 45
tree :n-1 :len
lf 90
tree :n-1 :len
rt 45
bk :n*:len
end
```

Example usage:
```tree 5 100```

### Serpinski triangle (Function):
func tri :n :len
if :n<1 [stop]
repeat 3 [tri :n-1 :len/2  fd :len rt 120]
end

Example usage:
```tri 5 320```

**Note: _:len_ variable should be at least 2^_:n_ multiplied by some other constant**

### SnowFlake (2 Seperate Functions):
```
func snow :n :len
repeat 3 [snow_aux :n :len rt 120]
end

func snow_aux :n :len
if :n<1 [fd :len stop]

snow_aux :n-1 :len/3
lf 60
snow_aux :n-1 :len/3
rt 120
snow_aux :n-1 :len/3
lf 60
snow_aux :n-1 :len/3
end
```
```
func snow :n :len
repeat 3 [snow_aux :n :len rt 120]
end
```

Example usage:
```snow 4 162```

**Note: _:len_ variable should be at least 3^_:n_ multiplied by some other constant**
