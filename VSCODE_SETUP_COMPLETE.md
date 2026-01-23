# OpenOLAT VS Code Setup - Quick Reference

## ✅ Setup Complete!

Your OpenOLAT development environment is fully configured and running.

## 🎯 Current Status

- **Tomcat**: Running on port 8080 with debug mode on port 5005
- **PostgreSQL**: Running on port 5432
- **OpenOLAT WAR**: Deployed to Tomcat
- **Debug Mode**: ENABLED (listening on port 5005)

## 🚀 Quick Commands

### Start/Stop Tomcat
```bash
# Start with debug mode
export CATALINA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -XX:+UseG1GC -Xms256m -Xmx1024m -Djava.awt.headless=true"
/opt/homebrew/Cellar/tomcat@10/10.1.50/libexec/bin/startup.sh

# Stop
/opt/homebrew/Cellar/tomcat@10/10.1.50/libexec/bin/shutdown.sh
```

### View Logs
```bash
# Real-time logs
tail -f /opt/homebrew/Cellar/tomcat@10/10.1.50/libexec/logs/catalina.out

# Recent errors
tail -100 /opt/homebrew/Cellar/tomcat@10/10.1.50/libexec/logs/catalina.out | grep ERROR
```

### Rebuild & Redeploy
```bash
cd /Users/lichee/Documents/GitHub/OPENOLAT/OpenOLAT

# Build
mvn clean package -DskipTests

# Stop Tomcat
/opt/homebrew/Cellar/tomcat@10/10.1.50/libexec/bin/shutdown.sh

# Remove old deployment
rm -rf /opt/homebrew/Cellar/tomcat@10/10.1.50/libexec/webapps/openolat-lms-20.2-SNAPSHOT*

# Copy new WAR
cp target/openolat-lms-20.2-SNAPSHOT.war /opt/homebrew/Cellar/tomcat@10/10.1.50/libexec/webapps/

# Start Tomcat
export CATALINA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -XX:+UseG1GC -Xms256m -Xmx1024m -Djava.awt.headless=true"
/opt/homebrew/Cellar/tomcat@10/10.1.50/libexec/bin/startup.sh
```

## 🐛 Debugging in VS Code

### Step 1: Attach Debugger
1. Press **Cmd+Shift+D** (Run & Debug panel)
2. Select **"Debug (Attach) - Tomcat"**
3. Press **F5**
4. Wait for "Debugger attached" message

### Step 2: Set Breakpoints
- Open any Java file
- Click in the gutter (left of line numbers)
- Red dot appears = breakpoint set

### Step 3: Use the App
- Open browser: `http://localhost:8080/openolat-lms-20.2-SNAPSHOT`
- Trigger the code path with your breakpoint
- VS Code will pause execution

### Debug Shortcuts
- **F10**: Step Over
- **F11**: Step Into
- **Shift+F11**: Step Out
- **F5**: Continue
- **Shift+F5**: Stop Debugging

## 📂 Important Paths

| Item | Path |
|------|------|
| OpenOLAT Source | `/Users/lichee/Documents/GitHub/OPENOLAT/OpenOLAT` |
| Tomcat Home | `/opt/homebrew/Cellar/tomcat@10/10.1.50/libexec` |
| Tomcat Webapps | `/opt/homebrew/Cellar/tomcat@10/10.1.50/libexec/webapps` |
| Tomcat Logs | `/opt/homebrew/Cellar/tomcat@10/10.1.50/libexec/logs` |
| Config File | `src/main/java/olat.local.properties` |
| Data Directory | `/Users/lichee/.openolat/data` |
| PostgreSQL Data | `/opt/homebrew/var/postgresql@17` |

## 🔧 Configuration Files

### Database (olat.local.properties)
```properties
db.vendor=postgresql
db.name=openolat
db.user=openolat
db.pass=openolat
db.host=localhost
db.host.port=5432
```

### Tomcat Debug (setenv.sh)
Located at: `/opt/homebrew/opt/tomcat@10/bin/setenv.sh`
```bash
export CATALINA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -XX:+UseG1GC -Xms256m -Xmx1024m -Djava.awt.headless=true"
```

## 🌐 Access URLs

- **OpenOLAT Application**: http://localhost:8080/openolat-lms-20.2-SNAPSHOT
- **Tomcat Manager**: http://localhost:8080/manager/html
- **Debug Port**: localhost:5005

## 🔍 Troubleshooting

### Tomcat won't start - Port already in use
```bash
# Find and kill process on port 8080
lsof -ti:8080 | xargs kill -9

# Or use different port (edit server.xml in Tomcat conf/)
```

### Can't attach debugger
```bash
# Verify debug port is listening
lsof -i :5005

# Should show: java (Tomcat process)
# If not, check CATALINA_OPTS in logs
```

### Application won't deploy
```bash
# Check Tomcat logs for errors
tail -f /opt/homebrew/Cellar/tomcat@10/10.1.50/libexec/logs/catalina.out

# Common issues:
# - Missing database tables (run setupDatabase.sql)
# - Wrong database credentials (check olat.local.properties)
# - Insufficient memory (increase -Xmx in CATALINA_OPTS)
```

### Database connection errors
```bash
# Check PostgreSQL is running
brew services list | grep postgresql

# Start if stopped
brew services start postgresql@17

# Test connection
/opt/homebrew/opt/postgresql@17/bin/psql -U openolat -d openolat -c "SELECT version();"
```

## 📚 Additional Resources

- **VS Code Debugging Guide**: [.vscode/DEBUGGING_GUIDE.md](.vscode/DEBUGGING_GUIDE.md)
- **VS Code Launch Config**: [.vscode/launch.json](.vscode/launch.json)
- **OpenOLAT README**: [README.md](README.md)
- **Tomcat Documentation**: https://tomcat.apache.org/tomcat-10.1-doc/

## 💡 Pro Tips

1. **Hot Reload**: Use `olat.debug=true` in `olat.local.properties` for Velocity template hot reload
2. **Faster Builds**: Use `mvn compile` instead of full package during development
3. **IDE Integration**: Use the Java Language Server for better code navigation
4. **Log Levels**: Adjust in `src/main/resources/log4j2.xml` for more/less verbose logging

---

*Last Updated: January 18, 2026*
