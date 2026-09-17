import firebase_admin
from firebase_admin import credentials, firestore
import csv
import os

if not firebase_admin._apps:
    cred = credentials.Certificate("serviceAccountKey.json")
    firebase_admin.initialize_app(cred)

db = firestore.client()

files_to_upload = [
    "fixed_olevel_arts.csv",
    "fixed_olevel_science.csv",
    "fixed_alevel_arts.csv",
    "fixed_alevel_science.csv"
]

total_uploaded = 0

print("🚀 Starting upload to Firebase...\n")

for filename in files_to_upload:
    if os.path.exists(filename):
        with open(filename, mode='r', encoding='utf-8') as file:
            reader = csv.DictReader(file)
            for row in reader:
                db.collection('questions').add({
                    'level': row['level'],
                    'stream': row['stream'],
                    'subject': row['subject'],
                    'topic': row['topic'],
                    'year': int(row['year']),
                    'paper': row['paper'],
                    'questionText': row['questionText'],
                    'optionA': row['optionA'],
                    'optionB': row['optionB'],
                    'optionC': row['optionC'],
                    'optionD': row['optionD'],
                    'correctOption': row['correctOption'],
                    'explanation': row['explanation']
                })
                total_uploaded += 1
        print(f"✅ Uploaded {filename}")
    else:
        print(f"⚠️ Could not find {filename}.")

print(f"\n🎉 SUCCESS! Total questions uploaded to Firebase: {total_uploaded}")