Author: Ryan Morrissey
Intro to IS Lab 1: Year-round Orienteering
Due: 10/26/2017 11:59PM
This program is meant as a pathfinding tool for orienteering.
The user will supply a png of the map, a text file of the elevation information,
a text file of the course they need to take, and finally what season it is.

usage:  javac Project1.java
        java Project1

Alright, I am not very familiar with project writeups so I will try to include
as much information as possible.

First, the heuristic.
I needed a value for the distance to adjacent nodes.  The X and Y cost were given,
so I calculated the D value and hardcoded it in.  Each node calculates its distance
as this, the constant X/Y/D value multiplied by two scalars.  The first scalar
is node type.  The node types are as followed.
Open Land - 1
Rough Meadow - 1.4
Easy Movement Forest - 1.15
Slow Run Forest - 1.35
Walk Forest - 1.5
Impassible Vegetation - 100
Lake/Swamp/Marsh - 50
Paved Road - 1
Footpath - 1
Out of Bounds - 1000000000

I tried to think of how much slower it would be to travel through these,
and attempted to give it a value.  I thought that I would much sooner travel
through a water node than an impassible vegetation node, so I gave it a much
higher value.  The reason as to why out of bounds is such a high number, was
because of my implementation of A*.  I considered having that node be null,
but I figured that I did not want to keep null checking, and just gave it
and impossibly high scalar to make it so the program will never conisder it.

I then found a scalar for the elevation differences.  This one could easily be
changed, but it was the one I had the least understanding in.  To implement it,
I had the current node elevation subtract the target node elevation.
I ended up using something like this,
<-2   -2   -1    0   1   2  >2
1.5   1.3   1.1  0  .9  .7  .5
If I was going uphill, it would take longer between 0 and -1, -1 and -2, and a constant
for anything lower than -2.  The opposite is true for uphill, where it would reduce
in weight as I thought about going faster going downhill.

Now, for seasons.  Seasons were very easy to implement.
For Summer I did nothing and never considered it.

For Fall I just checked if a footpath was next to a easy movement forest, and
changed its difficulty scalar to match the forest.

For Winter, I checked to see if a water node was next to a non-water node, then
I would call a function called freeze that would go a distance of 7 and "freeze"
the water, changing that nodes difficulty scalar to 1.15, which is severely reduced
from its previous 50.  I figured it might be a little slippery, so it was still
a bit slower than walking on a normal path.  If I froze a node, I would then
change the RGB of the terrain file to a light blue, to show that the water is frozen.

For Spring, I did the same exact thing as Winter.  I called a swampify method
and checked its elevation to see if I should change it.  I decided to change
the terrain file to look blue rather than a new color, because seeing the map flooded
looked kinda cool.

Finally, for output.
I did two things, print a red course to show visually what the path is, as
well as print out the paths between each and every node.  One thing that is very
important to note, even though it follows the course from control to control, it
prints it out backwards.  Thats because I programmed it to follow the parent line,
and changing it so it printed normally would require more rewriting than I wanted
as the program was running just as I intended.  Since the visual output looked
good, I decided to overlook this.

And overall thats about it.  I already had experience with programming Djikstras
or however you spell it, so changing it to A* was not difficult.  I did hard-code
394 and 499 some times as I was focused only on the input given on the labs
write up, but I am planning on changing it to be more abstract.  It works just fine,
but I don't think its the best form and I would like to improve on it.

Overall I am very happy with this program.  It was a lot of fun to do.  I have to
say though, I had so much trouble at the start because I misunderstood how
the terrain image is navigated.  I ended up just flipping the x and y coordinate
so I could think of it as a traditional programming array.  When I started to
read the course info, I also did not know that its X and Ys were backwards too,
so it maaaaaaaaaaaay have lead to an hour of frustrated debugging to find out why
I was going out of bounds during my pathfinding, and why things were not connected
like the way I wanted.
