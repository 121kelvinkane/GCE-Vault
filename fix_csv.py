import csv
import os

# Map filenames to their correct stream
files_to_fix = {
    "olevel_arts.csv": "Arts",
    "olevel_science.csv": "Science",
    "alevel_arts.csv": "Arts",
    "alevel_science.csv": "Science"
}

for filename, stream in files_to_fix.items():
    if not os.path.exists(filename):
        print(f"⚠️ Could not find {filename}. Skipping.")
        continue
        
    # Read original data
    with open(filename, mode='r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        original_fieldnames = reader.fieldnames
        rows = list(reader)
    
    # Define new fieldnames with 'stream' inserted after 'level'
    new_fieldnames = ['level', 'stream', 'subject', 'topic', 'year', 'paper', 
                      'questionText', 'optionA', 'optionB', 'optionC', 'optionD', 
                      'correctOption', 'explanation']
    
    # Add the stream value to every row
    for row in rows:
        row['stream'] = stream
        
    # Write to a new fixed file
    fixed_filename = f"fixed_{filename}"
    with open(fixed_filename, mode='w', encoding='utf-8', newline='') as file:
        writer = csv.DictWriter(file, fieldnames=new_fieldnames)
        writer.writeheader()
        writer.writerows(rows)
        
    print(f"✅ Fixed {filename} -> Saved as {fixed_filename} ({len(rows)} questions)")

print("\n🎉 All CSV files are now ready for Firebase!")