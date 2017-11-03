import org.mockito.Mockito

import static liveplugin.PluginUtil.show

// add-to-classpath $HOME/.m2/repository/org/mockito/mockito-all/1.9.5/mockito*.jar

if (!isIdeStartup) {
	show(Mockito.class.name)
}

// To add jars or folder with class files to plugin classloader:
// - After "import" statements add one or more lines which start with "// add-to-classpath " followed by path.
// - Path must have "/" separator and can include glob wildcards if you want to include multiple jars.
// - OS environment variables can be used with '$' as prefix (e.g. "$JAVA_HOME")
//   (additional $PLUGIN_PATH variable contains path to current plugin folder).
// - Note that in plugin.groovy files there is intention to insert "add-to-classpath" comment
//   (use alt+Enter to see popup with intentions)
//
// Note that new classloader is created each time plugin is executed.
// It means that addition classpath library classes are loaded on each plugin execution and
// won't be unloaded if there are objects referencing them or classloader.
// See also https://docs.oracle.com/javase/specs/jls/se7/html/jls-12.html#jls-12.7
