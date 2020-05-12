This file was originally authored by Roberto Tyley. Some changes have been made to reflect this project's new name
(Gitclean) and home.

Gitclean is written in Scala, a modern functional language that runs on the JVM - so it
can run anywhere Java can.

Here's a rough set of instructions for building Gitclean, if you don't want to use the
pre-built [downloads](https://github.com/youngde811/gitclean/#download):

* Install Java JDK 8 or above
* Install [sbt](https://www.scala-sbt.org/1.x/docs/Setup.html)
* `git clone git@github.com:youngde811/gitclean.git`
* `cd gitclean`
* `sbt`<- start the sbt console
* `gitclean/assembly` <- download dependencies, run the tests, build the jar

To find the jar once it's built, just look at the last few lines of output from the
`assembly` task - it'll say something like this:

```
[info] Packaging /Users/young/devel/gitclean/gitclean/target/gitclean-1.11.9-SNAPSHOT-master-21d2115.jar ...
[info] Done packaging.
[success] Total time: 19 s, completed 26-Sep-2014 16:05:11
```

For changes to the Scala code, I use Emacs. Alternatively, you may want to use IntelliJ and its Scala plugin to help
with the Scala syntax.

Note: see Coursera's [online Scala course](https://www.coursera.org/course/progfun) for help learning Scala.
