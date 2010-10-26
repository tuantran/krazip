As a "contrib" plugin, this plugin is not bundled with CruiseControl automatically.
You may, however, build this plugin yourself and register it with your CruiseControl
server. To do this, you must put the compiled plugin on your CruiseControl server's
classpath. Then, either add the plugin's XML node name and class name to the
main/src/net/sourceforge/cruisecontrol/default-plugins.properties file or register
it in your server's config.xml file using a <plugin> tag. See
http://cruisecontrol.sourceforge.net/main/plugins.html#registration for more
information.


Install instruction :

1.) Put krazip*.jar and irclib.jar into CruiseCrontrol lib folder
2.) Register the plugin in config.html (see below for instruction)
3.) Done!

Note: Krazip requires IRClib for working.  (http://moepii.sourceforge.net/)

Example configuration for config.xml :

<cruisecontrol>

...

    <project name="connectfour">

        <plugin name="krazip" classname="net.sourceforge.cruisecontrol.publishers.KrazipIRCPublisher"/>

        ...

        <publishers>
	        <krazip host="irc.linpro.no"
               port="6667"
               channel="#krazip"
               resulturl="http://localhost:8080/cruisecontrol/buildresults/${project.name}" />
        </publishers>

        ...

    </project>

</cruisecontrol>
