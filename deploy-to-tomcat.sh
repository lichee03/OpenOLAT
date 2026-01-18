#!/bin/bash
# OpenOLAT Complete Setup and Deployment Script for VS Code + Tomcat

set -e  # Exit on error

echo "🚀 OpenOLAT Setup Script for VS Code + Tomcat"
echo "================================================"

# Configuration
OPENOLAT_DIR="/Users/lichee/Documents/GitHub/OPENOLAT/OpenOLAT"
TOMCAT_DIR="/opt/homebrew/opt/tomcat@10"
TOMCAT_WEBAPPS="$TOMCAT_DIR/libexec/webapps"
POSTGRES_BIN="/opt/homebrew/opt/postgresql@17/bin"

echo ""
echo "📋 Configuration:"
echo "   OpenOLAT: $OPENOLAT_DIR"
echo "   Tomcat:   $TOMCAT_DIR"
echo "   PostgreSQL: $POSTGRES_BIN"
echo ""

# Step 1: Check if build is complete
echo "⏳ Checking build status..."
if [ -f "$OPENOLAT_DIR/target/openolat-lms-20.2-SNAPSHOT.war" ]; then
    echo "✅ WAR file already exists!"
else
    echo "⚙️  Building OpenOLAT (this may take 5-10 minutes)..."
    cd "$OPENOLAT_DIR"
    mvn clean package -DskipTests
    
    if [ $? -ne 0 ]; then
        echo "❌ Build failed! Check errors above."
        exit 1
    fi
fi

# Step 2: Stop Tomcat if running
echo ""
echo "🛑 Stopping Tomcat (if running)..."
$TOMCAT_DIR/bin/shutdown.sh 2>/dev/null || echo "   Tomcat was not running"

# Step 3: Clean old deployment
echo "🧹 Cleaning old deployment..."
rm -rf "$TOMCAT_WEBAPPS/openolat-lms-20.2-SNAPSHOT"
rm -f "$TOMCAT_WEBAPPS/openolat-lms-20.2-SNAPSHOT.war"

# Step 4: Deploy new WAR
echo "📦 Deploying WAR file to Tomcat..."
cp "$OPENOLAT_DIR/target/openolat-lms-20.2-SNAPSHOT.war" "$TOMCAT_WEBAPPS/"

# Step 5: Create data directory
echo "📁 Creating data directory..."
sudo mkdir -p /opt/openolat/olatdata
sudo chown $(whoami) /opt/openolat/olatdata

# Step 6: Start Tomcat in debug mode
echo "🔧 Starting Tomcat in DEBUG mode..."
export CATALINA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -XX:+UseG1GC -Xms256m -Xmx1024m -Djava.awt.headless=true"
$TOMCAT_DIR/bin/startup.sh

echo ""
echo "✅ Setup Complete!"
echo ""
echo "📝 Next Steps:"
echo "   1. Wait 30 seconds for Tomcat to deploy the WAR"
echo "   2. Open VS Code and press Cmd+Shift+D"
echo "   3. Select 'Debug (Attach) - Tomcat'"
echo "   4. Press F5 to attach debugger"
echo "   5. Open browser: http://localhost:8080/openolat-lms-20.2-SNAPSHOT"
echo ""
echo "📊 Logs:"
echo "   tail -f $TOMCAT_DIR/libexec/logs/catalina.out"
echo ""
