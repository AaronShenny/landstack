from flask import Flask, jsonify, request
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

# Mock Record of Rights Database (Keyed by ULPIN)
# Notice how the keys are completely non-standard (e.g. 'owner_name' instead of 'ownerName')
# This proves the Spring Boot Dynamic Field Mapper works!
MOCK_DATABASE = {
    "KL-001": {
        "owner_name": "Aaronshenny",
        "land_area_sqm": 450.5,
        "usage_category": "RESIDENTIAL",
        "last_survey_date": "2023-01-15",
        "tax_status": "PAID",
        "encumbrances": "NONE"
    },
    "KL-002": {
        "owner_name": "John Doe",
        "land_area_sqm": 1200.0,
        "usage_category": "AGRICULTURAL",
        "last_survey_date": "2022-11-20",
        "tax_status": "PENDING",
        "encumbrances": "BANK_LOAN"
    },
    "KL-003": {
        "owner_name": "Jane Smith",
        "land_area_sqm": 850.75,
        "usage_category": "COMMERCIAL",
        "last_survey_date": "2024-02-10",
        "tax_status": "PAID",
        "encumbrances": "NONE"
    },
    "KL-004": {
        "owner_name": "State Government",
        "land_area_sqm": 5000.0,
        "usage_category": "PUBLIC_UTILITY",
        "last_survey_date": "2020-05-01",
        "tax_status": "EXEMPT",
        "encumbrances": "NONE"
    },
    "KL-005": {
        "owner_name": "Alice Wonderland",
        "land_area_sqm": 250.0,
        "usage_category": "RESIDENTIAL",
        "last_survey_date": "2023-08-14",
        "tax_status": "PAID",
        "encumbrances": "DISPUTED"
    }
}

@app.route('/v1/land/<ulpin>/ror', methods=['GET'])
def get_record_of_rights(ulpin):
    """
    Simulates a State Government API endpoint returning Record of Rights data.
    """
    data = MOCK_DATABASE.get(ulpin)
    if data:
        return jsonify(data), 200
    else:
        return jsonify({"error": "ULPIN not found in State Records"}), 404

if __name__ == '__main__':
    print("Starting Mock State Node (Kerala) on port 5000...")
    app.run(port=5000, debug=True)
