Purpose
-------

This is a project to adapt the [Apache Karaf][karaf] [shell][karaf-shell] to non-OSGi, Spring-based* projects.
It is designed to be easily integrated into existing projects, providing either SSH access to a running application, 
or a direct console. Currently, my emphasis is to embed in server applications to allow SSH access, but it works as a
standalone app as well.

Very little of this project is original work; it's mainly a thin wrapper around Karaf.

* In principle, this could easily be adapted to Guice-based or other non-Spring-based projects. One of the core classes
here is blast.shell.CommandRegistry, which uses Spring to look up available commands at runtime. The normal Karaf
org.apache.karaf.shell.console.Main class uses a resource-loader-like discovery mechanism: define your classes in
"META-INF/services/org/apache/karaf/shell/commands", and it will pick them up and load them using ClassLoader.loadClass().
It would be simple to write a runtime registration option and Guice-based discovery mechanism as well.

Features
--------

* Easy extensibility -- easily add a new command for application-specific functions
* Tab-completion
* SSH access
* Simply customized branding (startup message, prompt)
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

Blast Shell is split into a number of modules, so you can embed as much or as little as you like. It's still not a lightweight
project, because of the volume of dependencies it totes along (all of Apache Karaf, which is doing the real work), but
much of that can be excluded.  I aim to have more intelligent maven exclusions in future versions.

### Embedding with Spring

See the SampleSshApp class in shell-samples for an example of embedded usage.  If you use Spring in your application,
you just need to include Blast Shell's Spring contexts, or copy the beans into your contexts.
By default, the Blast Shell beans expect a few properties to be defined and injected using Spring's PropertyPlaceholderConfigurer mechanism:

* welcome:  This is a string that users will see when they log in.  See an example in core/src/main/filtered-resources/branding.properties, or a prettier example in /org/apache/karaf/shell/console/branding.properties within the org.apache.karaf.shell.console-2.1.3.jar jar
* sshPort:  This is the port the SSH server will listen on.
* sshRealm:  This is used by the SSH server's authentication mechanism; it is only important if you're using JAAS authentication or something else that uses realms.
* hostKey:  The name of a file (like "host.key") that the SSH server will use as its host key.  It will generate a file of this name if it doesn't already exist.

The sample server loads a context file called property-context.xml which, in turn, loads a couple default property files (ssh.properties and branding.properties):

    <bean name="propertyPlaceholderConfigurer"
          class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <property name="locations">
            <util:list>
                <value>classpath:META-INF/shell/ssh.properties</value>
                <value>classpath:branding/branding.properties</value>
            </util:list>
        </property>
    </bean>

### Embedding with Guice
TBD...it's conceptually very similar to the Spring method, of course.
See above, in the "Purpose" section, for an overview of embedding without Spring.

### Embedding without Spring or Guice
Also TBD.  Again, see above, in the "Purpose" section, for an overview of embedding without Spring.


Extensibility
-------------
To add a new command, simply implement org.apache.felix.gogo.commands.Action and annotate it with org.apache.felix.gogo.commands.Command, as well as org.apache.felix.gogo.commands.Argument and org.apache.felix.gogo.commands.Option to mark options and arguments. 
Then register that bean with Spring.  Blast Shell will pick up any Spring bean implementing Action and expose it as an
action in the shell.


Implementation Notes
--------------------

blast-shell contains a "BlueprintContainerAwarePostProcessor", a Spring bean post-processor, which finds beans that implements
"BlueprintContainerAware" and feeds them the Spring application context, wrapped in a thin compatibility wrapper (SpringBackedBlueprintContainer).
This is kind of ironic, since BlueprintContainer was patterned after Spring originally. But it lets us use some Karaf
commands which are expecting a BlueprintContainer but are functional with a normal Spring context.

We also override Karaf's ShellFactory, pretty much only in order to customize the branding.


Similar Projects
----------------
If you're looking at this, be sure to also check out Sonatype's gshell project, also on Github. It bears some similarity
with the Karaf shell (there was some code sharing back in the day, but I don't know which direction it went) but meanwhile
they've made a lot of changes and improvements. Karaf, meanwhile, has changed as well, so at this point they just have
different feature sets.  Gshell has nested command sets that are really cool, for example. Karaf is (currently) easier to
embed -- as of this writing, Gshell doesn't support access via an SSH server. Gshell uses Guice, Blast Shell uses Spring.


License
-------

It is provided under the Apache license, the same as Karaf.

Why "Blast Shell"? 
------------------

I originally called this project "carapace", to be slightly more meaningful, but when it comes down to it "blast shell" had a better ring to it. And it lends itself to more entertaining icons. 

