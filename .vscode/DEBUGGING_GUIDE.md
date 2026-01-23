# VS Code Debugging Guide for OpenOLAT

## Quick Start

### 1. Enable Debug Mode in Tomcat

Before you can attach the debugger, Tomcat must be started with remote debugging enabled.

#### Option A: Using setenv.sh/setenv.bat (Recommended)

In your Tomcat `bin/` folder, edit or create the `setenv.sh` (Mac/Linux) or `setenv.bat` (Windows) file:

**Mac/Linux (`bin/setenv.sh`):**
```bash
export CATALINA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -XX:+UseG1GC -Xms256m -Xmx1024m -Djava.awt.headless=true"
```

**Windows (`bin/setenv.bat`):**
```bat
set CATALINA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -XX:+UseG1GC -Xms256m -Xmx1024m -Djava.awt.headless=true
```

#### Option B: Using catalina.sh/catalina.bat Directly

**Mac/Linux:**
```bash
./catalina.sh jpda start
```

**Windows:**
```bat
catalina.bat jpda start
```

### 2. Start Tomcat

Start Tomcat normally using the method above. You should see output indicating the debug port is listening:
```
Listening for transport dt_socket at address: 5005
```

### 3. Attach Debugger in VS Code

1. Open the **Run and Debug** panel (Cmd+Shift+D on Mac, Ctrl+Shift+D on Windows/Linux)
2. Select **"Debug (Attach) - Tomcat"** from the dropdown
3. Press **F5** or click the green play button
4. Wait for the debugger to attach (you'll see "Debugger attached" in the Debug Console)

### 4. Set Breakpoints

- Open any Java file in the OpenOLAT project
- Click in the gutter (left of line numbers) to set a red breakpoint
- When code execution hits that line, VS Code will pause and show variables, call stack, etc.

## Debug Configurations

### Debug (Attach) - Tomcat
Default configuration that attaches to Tomcat on port 5005.

### Debug (Attach) - Tomcat (Custom Port)
Prompts you to enter a custom debug port if you're using a different port than 5005.

## Common Issues

### "Failed to attach"
- **Cause:** Tomcat is not running with debug mode enabled
- **Solution:** Verify Tomcat is started with the debug flags (see Step 1 above)

### "Connection refused"
- **Cause:** Tomcat hasn't finished starting yet
- **Solution:** Wait a few more seconds and try attaching again

### Breakpoints show as gray/hollow circles
- **Cause:** Source code doesn't match deployed code
- **Solution:** 
  1. Rebuild the project: `mvn clean package -Pcompressjs,tomcat -DskipTests`
  2. Redeploy to Tomcat
  3. Restart Tomcat in debug mode
  4. Reattach debugger

### Hot Code Replace Failed
- **Cause:** You made changes that can't be hot-swapped (e.g., method signature changes)
- **Solution:** Restart Tomcat and reattach the debugger

## Tips

- **Use conditional breakpoints:** Right-click a breakpoint and add a condition (e.g., `userId == 123`)
- **Logpoints:** Right-click in the gutter and select "Add Logpoint" to log messages without stopping execution
- **Evaluate expressions:** While paused, hover over variables or use the Debug Console to evaluate expressions
- **Step controls:** 
  - F10 = Step Over
  - F11 = Step Into
  - Shift+F11 = Step Out
  - F5 = Continue

## Advanced Configuration

### Debug on a Different Port

If port 5005 is already in use, you can change it:

1. In Tomcat's `setenv.sh` or `setenv.bat`, change `address=*:5005` to `address=*:8000` (or any available port)
2. In VS Code, use the **"Debug (Attach) - Tomcat (Custom Port)"** configuration and enter your port number

### Debug with Suspend

To pause Tomcat on startup (useful for debugging initialization):

Change `suspend=n` to `suspend=y` in the Tomcat debug flags:
```bash
-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005
```

Tomcat will wait for the debugger to attach before proceeding.

## Resources

- [VS Code Java Debugging](https://code.visualstudio.com/docs/java/java-debugging)
- [Tomcat Remote Debugging](https://cwiki.apache.org/confluence/display/TOMCAT/Developing#Developing-Q9)
