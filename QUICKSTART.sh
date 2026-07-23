#!/bin/bash
# Quick Start Script for TimeSheet Management System

echo "================================"
echo "TimeSheet Management System"
echo "Quick Start Guide"
echo "================================"
echo ""

# Check prerequisites
echo "📋 Checking prerequisites..."
echo ""

if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 21+"
    exit 1
fi
echo "✅ Java: $(java -version 2>&1 | head -1)"

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven 3.8.9+"
    exit 1
fi
echo "✅ Maven: $(mvn -version 2>&1 | head -1)"

if ! command -v node &> /dev/null; then
    echo "❌ Node.js not found. Please install Node.js 14.18.0+"
    exit 1
fi
echo "✅ Node: $(node --version)"

if ! command -v npm &> /dev/null; then
    echo "❌ npm not found. Please install npm"
    exit 1
fi
echo "✅ npm: $(npm --version)"

echo ""
echo "================================"
echo "🚀 Starting Services"
echo "================================"
echo ""

# Start PostgreSQL and MailHog with Docker Compose
if command -v docker &> /dev/null && command -v docker-compose &> /dev/null; then
    echo "🐳 Starting PostgreSQL and MailHog with Docker Compose..."
    docker-compose up -d postgres mailhog
    echo "✅ Database: http://localhost:5432"
    echo "✅ MailHog UI: http://localhost:8025"
    echo ""
    sleep 5
else
    echo "⚠️  Docker not found. Please ensure PostgreSQL and MailHog are running:"
    echo "   PostgreSQL: localhost:5432 (user: tss_user, pass: tss_password, db: tss)"
    echo "   MailHog: localhost:1025 (UI: localhost:8025)"
    echo ""
fi

# Build Backend
echo "🔨 Building Backend..."
mvn clean package -DskipTests -q
if [ $? -eq 0 ]; then
    echo "✅ Backend built successfully"
    JAR_FILE=$(ls target/TimeSheet_Maangement_System-*.jar 2>/dev/null | head -1)
    if [ -n "$JAR_FILE" ]; then
        echo "   JAR: $JAR_FILE"
    fi
else
    echo "❌ Backend build failed"
    exit 1
fi
echo ""

# Start Backend
echo "📱 Starting Backend Server..."
java -jar target/TimeSheet_Maangement_System-*.jar > backend.log 2>&1 &
BACKEND_PID=$!
echo "✅ Backend starting (PID: $BACKEND_PID)..."
echo "   API: http://localhost:8080"
echo ""
sleep 5

# Install Frontend
echo "📦 Installing Frontend Dependencies..."
cd frontend
npm install -q
if [ $? -eq 0 ]; then
    echo "✅ Frontend dependencies installed"
else
    echo "❌ Frontend installation failed"
    kill $BACKEND_PID
    exit 1
fi
echo ""

# Start Frontend
echo "🎨 Starting Frontend Server..."
ng serve --port 4200 > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo "✅ Frontend starting (PID: $FRONTEND_PID)..."
echo "   UI: http://localhost:4200"
echo ""

# Summary
echo "================================"
echo "✨ All Services Started!"
echo "================================"
echo ""
echo "📍 Access the application at:"
echo "   Frontend: http://localhost:4200"
echo "   Backend API: http://localhost:8080"
echo "   MailHog: http://localhost:8025"
echo ""
echo "🔐 Test Credentials:"
echo "   Username: admin"
echo "   Password: password123"
echo ""
echo "📚 Documentation:"
echo "   - API Docs: API_DOCUMENTATION.md"
echo "   - Setup Guide: FRONTEND_SETUP.md"
echo "   - Completion Report: COMPLETION.md"
echo ""
echo "🛑 To stop services:"
echo "   kill $BACKEND_PID  # Stop backend"
echo "   kill $FRONTEND_PID # Stop frontend"
echo "   docker-compose down  # Stop Docker services"
echo ""
echo "📝 Logs:"
echo "   Backend: backend.log"
echo "   Frontend: frontend.log"
echo ""
