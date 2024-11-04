# app.py
from flask import Flask, render_template, request, send_file, jsonify
import os
import base64
from pathlib import Path

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB max file size
UPLOAD_FOLDER = 'uploads'
TEMPLATE_FOLDER = Path(__file__).parent / 'templates'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

def get_file_content(filename):
    file_path = TEMPLATE_FOLDER / filename
    if file_path.exists():
        with open(file_path, 'r', encoding='utf-8') as f:
            return f.read()
    return None

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/get_receive_html')
def get_receive_html():
    content = get_file_content('receive.html')
    if content:
        return jsonify({'content': content})
    return jsonify({'error': 'File not found'}), 404

@app.route('/get_send_html')
def get_send_html():
    content = get_file_content('send.html')
    if content:
        return jsonify({'content': content})
    return jsonify({'error': 'File not found'}), 404

@app.route('/send_file', methods=['POST'])
def send_file_handler():
    if 'file' not in request.files:
        return jsonify({'error': 'No file provided'}), 400
    
    file = request.files['file']
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400

    # Save and process file
    filepath = os.path.join(UPLOAD_FOLDER, file.filename)
    file.save(filepath)
    
    with open(filepath, 'rb') as f:
        base64_data = base64.b64encode(f.read()).decode('utf-8')
    
    os.remove(filepath)  # Clean up
    return jsonify({'base64': base64_data})

@app.route('/send_text', methods=['POST'])
def send_text_handler():
    data = request.json
    if not data or 'text' not in data:
        return jsonify({'error': 'No text provided'}), 400
    
    try:
        # Validate base64 content
        base64.b64decode(data['text'])
        return jsonify({'status': 'success'})
    except Exception as e:
        return jsonify({'error': 'Invalid base64 content'}), 400

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)