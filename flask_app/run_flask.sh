#!/bin/bash
# run_flask.sh

# Create virtual environment if it doesn't exist
if [ ! -d "venv" ]; then
    python3 -m venv venv
fi

# Activate virtual environment
source venv/bin/activate



# Run the Flask application
python app.py
