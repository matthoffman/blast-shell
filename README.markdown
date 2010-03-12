Purpose
-------

This is a project to adapt the [Apache Karaf][karaf] [shell][karaf-shell] to non-OSGi, Spring-based projects.  
It is designed to be easily integrated into existing projects, providing either SSH access to a running application, 
or a direct console.

Very little of this project is original work; it's mainly a wrapper around Karaf and Jline. 

Features
--------

* Easy extensibility -- easily add a new command for application-specific functions
* Tab-completion
* SSH access
* Simply customized branding
* Built-in commands, including grep, cat, each, if, exec, more, and sort
* Pretty colors


[karaf]: http://felix.apache.org/site/apache-felix-karaf.html
[karaf-shell]: http://felix.apache.org/site/41-console-and-commands.html


Starting the Shell Standalone
-----------------------------

Assuming you have Maven 2 and Java 1.6 or higher installed, go first to the root directory and run "mvn install", so that the jars are in your local Maven repository, and then go to the "sample" directory and run "mvn exec:java".

I'm not aware of anything here that requires Java 1.6, so you're welcome to experiment with requiring something less.

Integrating the Shell
--------------------

(TODO)


Extensibility
-------------
To add a new command, simply implement org.apache.felix.gogo.commands.Action and annotate it with org.apache.felix.gogo.commands.Command, as well as org.apache.felix.gogo.commands.Argument and org.apache.felix.gogo.commands.Option to mark options and arguments. 
Then register that bean with Spring.  Blast Shell will pick up any Spring bean implementing Action. 

License
-------

It is provided under the Apache license, the same as Karaf.

Why "Blast Shell"? 
------------------

I originally called this project "carapace", to be slightly more meaningful, but when it comes down to it "blast shell" had a better ring to it. And it lends itself to more entertaining icons. 

