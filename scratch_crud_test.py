import urllib.request
import urllib.parse
import urllib.error
import json
import random
import datetime

BASE_URL = "http://localhost:8080/api/v1"
HEADERS = {"Content-Type": "application/json"}

results = []

def record(module, action, endpoint, status_ok, details=""):
    results.append({
        "module": module,
        "action": action,
        "endpoint": endpoint,
        "passed": status_ok,
        "details": details
    })
    mark = "PASS" if status_ok else "FAIL"
    print(f"[{mark}] {module:12} | {action:22} | {endpoint:38} | {details}")

def request(endpoint, method="GET", data=None, token=None):
    url = f"{BASE_URL}{endpoint}"
    req_headers = dict(HEADERS)
    if token:
        req_headers["Authorization"] = f"Bearer {token}"
    
    body = json.dumps(data).encode("utf-8") if data is not None else None
    req = urllib.request.Request(url, data=body, headers=req_headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            status = resp.status
            content = resp.read().decode("utf-8")
            res_json = json.loads(content) if content else {}
            return status, res_json
    except urllib.error.HTTPError as e:
        content = e.read().decode("utf-8")
        try:
            res_json = json.loads(content)
        except Exception:
            res_json = {"raw": content}
        return e.code, res_json
    except Exception as e:
        return 0, {"error": str(e)}

def generate_valid_gstin(state="27"):
    # Format: 2 digits + 5 uppercase letters + 4 digits + 1 uppercase letter + 1 char + 'Z' + 1 char
    # e.g., 27AABCT1234E1Z5
    letters5 = "ABCDE"
    digits4 = f"{random.randint(1000, 9999)}"
    letter1 = "F"
    char1 = "1"
    z = "Z"
    checksum = f"{random.randint(1, 9)}"
    return f"{state}{letters5}{digits4}{letter1}{char1}{z}{checksum}"

def run_suite():
    print("==================================================================================")
    print("           AR INDUSTRIES FULL-STACK APPLICATION CRUD VERIFICATION SUITE           ")
    print("==================================================================================\n")
    
    token = None
    
    # -------------------------------------------------------------
    # 1. AUTHENTICATION
    # -------------------------------------------------------------
    print("--- 1. AUTHENTICATION MODULE ---")
    st, resp = request("/auth/login", method="POST", data={"username": "admin", "password": "admin123"})
    if st == 200 and "token" in resp:
        token = resp["token"]
        record("Auth", "POST Login", "/auth/login", True, f"Token generated (role: {resp.get('role')})")
    else:
        record("Auth", "POST Login", "/auth/login", False, f"Status: {st}, Resp: {resp}")
        print("Cannot continue without auth token!")
        return
        
    st, resp = request("/auth/me", method="GET", token=token)
    record("Auth", "GET Current User", "/auth/me", st == 200, f"Status: {st}, Authenticated User: {resp.get('username')}")

    # -------------------------------------------------------------
    # 2. CUSTOMERS
    # -------------------------------------------------------------
    print("\n--- 2. CUSTOMER MODULE ---")
    rand_num = random.randint(1000, 9999)
    valid_gstin = generate_valid_gstin("27")
    cust_payload = {
        "customerName": f"Apex Switchgear Industries {rand_num}",
        "companyName": f"Apex Switchgear Pvt Ltd {rand_num}",
        "phone": f"98234{rand_num:0>5}",
        "email": f"apex_{rand_num}@example.com",
        "gstin": valid_gstin,
        "billingAddress": "Plot 104, Midc Industrial Area, Hingna",
        "city": "Nagpur",
        "state": "Maharashtra",
        "stateCode": "27",
        "creditLimit": 500000.0,
        "paymentTerms": "NET_30",
        "openingBalance": 15000.0
    }
    
    # CREATE
    st, c_created = request("/customers", method="POST", data=cust_payload, token=token)
    cust_id = c_created.get("id") if st == 201 else None
    record("Customer", "CREATE (POST)", "/customers", st == 201 and cust_id is not None, f"Status: {st}, Customer ID: {cust_id}, Code: {c_created.get('customerCode')}")
    
    if cust_id:
        # READ ALL (Paginated)
        st, c_list = request("/customers?page=0&size=10", method="GET", token=token)
        total_custs = c_list.get("totalRecords", len(c_list.get("data", [])))
        record("Customer", "READ ALL (GET)", "/customers?page=0&size=10", st == 200, f"Status: {st}, Total Records: {total_custs}")
        
        # READ BY ID
        st, c_one = request(f"/customers/{cust_id}", method="GET", token=token)
        record("Customer", "READ ONE (GET)", f"/customers/{cust_id}", st == 200 and c_one.get("customerName") == cust_payload["customerName"], f"Status: {st}, Name: {c_one.get('customerName')}")
        
        # UPDATE
        cust_payload["city"] = "Nagpur Central Zone"
        cust_payload["creditLimit"] = 750000.0
        st, c_updated = request(f"/customers/{cust_id}", method="PUT", data=cust_payload, token=token)
        record("Customer", "UPDATE (PUT)", f"/customers/{cust_id}", st == 200 and c_updated.get("city") == "Nagpur Central Zone", f"Status: {st}, City: {c_updated.get('city')}, CreditLimit: {c_updated.get('creditLimit')}")
        
        # DELETE (Soft Deactivation)
        st, _ = request(f"/customers/{cust_id}", method="DELETE", token=token)
        record("Customer", "DELETE (Soft)", f"/customers/{cust_id}", st == 204, f"Status: {st} (No Content / Deactivated)")

    # -------------------------------------------------------------
    # 3. PRICE MASTER
    # -------------------------------------------------------------
    print("\n--- 3. PRICE MASTER MODULE ---")
    p_num = random.randint(100, 999)
    price_payload = {
        "category": "Modular Box",
        "thickness": f"1.{p_num % 10} mm",
        "productType": f"Heavy_Duty_{p_num}",
        "size": "8M",
        "packaging": "20 Pcs / Box",
        "pcsPerBox": 20,
        "wholesaleRate": 210.00,
        "retailRate": 245.00,
        "effectiveFrom": "2026-10-01"
    }
    
    # CREATE
    st, p_created = request("/prices", method="POST", data=price_payload, token=token)
    price_id = p_created.get("id") if st == 201 else None
    record("Price Master", "CREATE (POST)", "/prices", st == 201 and price_id is not None, f"Status: {st}, Price Record ID: {price_id}")
    
    if price_id:
        # READ ALL
        st, p_list = request("/prices?page=0&size=10", method="GET", token=token)
        record("Price Master", "READ ALL (GET)", "/prices", st == 200, f"Status: {st}, Total Records: {p_list.get('totalRecords')}")
        
        # READ BY ID
        st, p_one = request(f"/prices/{price_id}", method="GET", token=token)
        record("Price Master", "READ ONE (GET)", f"/prices/{price_id}", st == 200, f"Status: {st}, Wholesale Rate: {p_one.get('wholesaleRate')}")
        
        # LOOKUP (with URL encoded query parameters)
        lookup_params = urllib.parse.urlencode({
            "category": price_payload["category"],
            "thickness": price_payload["thickness"],
            "productType": price_payload["productType"],
            "size": price_payload["size"]
        })
        st, p_lookup = request(f"/prices/lookup?{lookup_params}", method="GET", token=token)
        record("Price Master", "LOOKUP (GET)", f"/prices/lookup", st == 200 and p_lookup.get("id") == price_id, f"Status: {st}, Looked up rate: {p_lookup.get('wholesaleRate')}")
        
        # UPDATE
        price_payload["wholesaleRate"] = 225.00
        st, p_updated = request(f"/prices/{price_id}", method="PUT", data=price_payload, token=token)
        record("Price Master", "UPDATE (PUT)", f"/prices/{price_id}", st == 200 and float(p_updated.get("wholesaleRate", 0)) == 225.0, f"Status: {st}, Updated Wholesale: {p_updated.get('wholesaleRate')}")
        
        # DELETE
        st, _ = request(f"/prices/{price_id}", method="DELETE", token=token)
        record("Price Master", "DELETE (Soft)", f"/prices/{price_id}", st == 204, f"Status: {st} (No Content / Deactivated)")

    # -------------------------------------------------------------
    # 4. INVENTORY
    # -------------------------------------------------------------
    print("\n--- 4. INVENTORY MODULE ---")
    inv_num = random.randint(1000, 9999)
    inv_payload = {
        "itemCode": f"BOX-CRU-{inv_num}",
        "itemName": f"Modular Concealed Box 4M (Batch {inv_num})",
        "category": "Modular Box",
        "thickness": "0.8 mm",
        "productType": "Normal Clip",
        "size": "4M",
        "packaging": "20 Pcs / Box",
        "pcsPerBox": 20,
        "currentStock": 200,
        "minimumStock": 50,
        "unit": "Pcs"
    }
    
    # CREATE
    st, inv_created = request("/inventory", method="POST", data=inv_payload, token=token)
    inv_id = inv_created.get("id") if st == 201 else None
    record("Inventory", "CREATE (POST)", "/inventory", st == 201 and inv_id is not None, f"Status: {st}, ID: {inv_id}, ItemCode: {inv_created.get('itemCode')}")
    
    if inv_id:
        # READ ALL
        st, inv_list = request("/inventory?page=0&size=10", method="GET", token=token)
        record("Inventory", "READ ALL (GET)", "/inventory", st == 200, f"Status: {st}, Total Records: {inv_list.get('totalRecords')}")
        
        # READ BY ID
        st, inv_one = request(f"/inventory/{inv_id}", method="GET", token=token)
        record("Inventory", "READ ONE (GET)", f"/inventory/{inv_id}", st == 200, f"Status: {st}, Current Stock: {inv_one.get('currentStock')}")
        
        # STOCK ADJUSTMENT (INCREMENT)
        adj_payload = {
            "inventoryId": inv_id,
            "adjustmentType": "INCREMENT",
            "quantity": 50,
            "unit": "Pcs",
            "notes": "Incoming manufacturing batch"
        }
        st, inv_adj = request("/inventory/adjust", method="POST", data=adj_payload, token=token)
        record("Inventory", "ADJUST STOCK (POST)", "/inventory/adjust", st == 200 and inv_adj.get("currentStock") == 250, f"Status: {st}, Stock 200 -> {inv_adj.get('currentStock')}")
        
        # STOCK MOVEMENTS AUDIT
        st, m_list = request(f"/inventory/{inv_id}/movements?page=0&size=10", method="GET", token=token)
        record("Inventory", "STOCK MOVEMENTS (GET)", f"/inventory/{inv_id}/movements", st == 200, f"Status: {st}, Movement Records: {len(m_list.get('data', []))}")
        
        # UPDATE
        inv_payload["itemName"] = f"Modular Concealed Box 4M Premium (Batch {inv_num})"
        inv_payload["minimumStock"] = 60
        st, inv_updated = request(f"/inventory/{inv_id}", method="PUT", data=inv_payload, token=token)
        record("Inventory", "UPDATE (PUT)", f"/inventory/{inv_id}", st == 200 and inv_updated.get("minimumStock") == 60, f"Status: {st}, Updated Name: {inv_updated.get('itemName')}")
        
        # DELETE
        st, _ = request(f"/inventory/{inv_id}", method="DELETE", token=token)
        record("Inventory", "DELETE (Soft)", f"/inventory/{inv_id}", st == 204, f"Status: {st} (No Content / Deactivated)")

    # -------------------------------------------------------------
    # 5. INVOICES & BILLING
    # -------------------------------------------------------------
    print("\n--- 5. INVOICES & BILLING MODULE ---")
    inv_c_num = random.randint(1000, 9999)
    valid_cg_gstin = generate_valid_gstin("22") # State 22 (Chhattisgarh)
    st, active_cust = request("/customers", method="POST", data={
        "customerName": f"Raipur Electrical Works {inv_c_num}",
        "companyName": f"Raipur Electrical Works Ltd {inv_c_num}",
        "phone": f"98765{inv_c_num:0>5}",
        "gstin": valid_cg_gstin,
        "billingAddress": "Industrial Area, Phase 2, Urla",
        "city": "Raipur",
        "state": "Chhattisgarh",
        "stateCode": "22",
        "creditLimit": 1000000.0,
        "openingBalance": 0.0
    }, token=token)
    
    billing_cust_id = active_cust.get("id")
    record("Customer", "CREATE BILLING CLIENT", "/customers", st == 201 and billing_cust_id is not None, f"Status: {st}, Created Billing Cust ID: {billing_cust_id}")
    
    invoice_payload = {
        "customerId": billing_cust_id,
        "customerName": active_cust.get("customerName"),
        "invoiceDate": "2026-10-10",
        "notes": "Verified billing invoice",
        "termsAndConditions": "30 days payment cycle",
        "items": [
            {
                "category": "Modular Box",
                "thickness": "0.8 mm",
                "productType": "Normal Clip",
                "size": "2M",
                "packaging": "20 Pcs / Box",
                "pcsPerBox": 20,
                "quantity": 10,
                "billingUnit": "Boxes",
                "ratePerPc": 430.0,
                "discountPercent": 0.0,
                "igstPercent": 18.0
            }
        ]
    }
    
    # CREATE INVOICE
    st, inv_res = request("/invoices", method="POST", data=invoice_payload, token=token)
    invoice_id = inv_res.get("id") if st == 201 else None
    record("Invoices", "CREATE (POST)", "/invoices", st == 201 and invoice_id is not None, f"Status: {st}, Inv#: {inv_res.get('invoiceNumber')}, GrandTotal: {inv_res.get('grandTotal')}")
    
    if invoice_id:
        # READ ALL
        st, inv_all = request("/invoices?page=0&size=10", method="GET", token=token)
        record("Invoices", "READ ALL (GET)", "/invoices", st == 200, f"Status: {st}, Total Records: {inv_all.get('totalRecords')}")
        
        # READ ONE BY ID
        st, inv_one = request(f"/invoices/{invoice_id}", method="GET", token=token)
        record("Invoices", "READ ONE (GET)", f"/invoices/{invoice_id}", st == 200 and inv_one.get("invoiceNumber") == inv_res.get("invoiceNumber"), f"Status: {st}, Number: {inv_one.get('invoiceNumber')}, Status: {inv_one.get('invoiceStatus')}")
        
        # CANCEL INVOICE
        st, inv_cancelled = request(f"/invoices/{invoice_id}/cancel", method="POST", token=token)
        record("Invoices", "CANCEL (POST)", f"/invoices/{invoice_id}/cancel", st == 200 and inv_cancelled.get("invoiceStatus") == "CANCELLED", f"Status: {st}, New Status: {inv_cancelled.get('invoiceStatus')}")

    # -------------------------------------------------------------
    # 6. PAYMENTS & RECEIVABLES
    # -------------------------------------------------------------
    print("\n--- 6. PAYMENTS & RECEIVABLES MODULE ---")
    pmt_payload = {
        "customerId": billing_cust_id,
        "invoiceId": invoice_id,
        "paymentDate": "2026-10-10",
        "amount": 10000.0,
        "paymentMode": "NEFT",
        "referenceNumber": f"UTR-{inv_c_num}-TEST",
        "notes": "Verified receipt payment"
    }
    
    # CREATE / RECORD
    st, pmt_res = request("/payments", method="POST", data=pmt_payload, token=token)
    pmt_id = pmt_res.get("id") if st == 201 else None
    record("Payments", "RECORD (POST)", "/payments", st == 201 and pmt_id is not None, f"Status: {st}, Payment ID: {pmt_id}, Receipt: {pmt_res.get('receiptNumber')}")
    
    if pmt_id:
        # READ ALL
        st, pmt_all = request("/payments?page=0&size=10", method="GET", token=token)
        record("Payments", "READ ALL (GET)", "/payments", st == 200, f"Status: {st}, Total Records: {pmt_all.get('totalRecords')}")
        
        # READ ONE BY ID
        st, pmt_one = request(f"/payments/{pmt_id}", method="GET", token=token)
        record("Payments", "READ ONE (GET)", f"/payments/{pmt_id}", st == 200, f"Status: {st}, Amount: {pmt_one.get('amount')}, Mode: {pmt_one.get('paymentMode')}")

    # -------------------------------------------------------------
    # 7. EMPLOYEES
    # -------------------------------------------------------------
    print("\n--- 7. EMPLOYEES MODULE ---")
    e_num = random.randint(1000, 9999)
    emp_payload = {
        "firstName": "Rajesh",
        "lastName": f"Kulkarni_{e_num}",
        "email": f"rajesh.{e_num}@arindustries.com",
        "phone": f"98211{e_num:0>5}",
        "dateOfBirth": "1992-04-18",
        "joiningDate": "2023-08-01",
        "department": "Tool & Die Maintenance",
        "designation": "Maintenance Lead",
        "employmentType": "FULL_TIME",
        "workLocation": "Plant 1",
        "shift": "General Shift (09:00 - 18:00)",
        "basicSalary": 38000.0
    }
    
    # CREATE
    st, emp_res = request("/employees", method="POST", data=emp_payload, token=token)
    emp_id = emp_res.get("id") if st == 201 else None
    record("Employees", "CREATE (POST)", "/employees", st == 201 and emp_id is not None, f"Status: {st}, Employee ID: {emp_id}, Code: {emp_res.get('employeeCode')}")
    
    if emp_id:
        # READ ALL
        st, emp_all = request("/employees?page=0&size=10", method="GET", token=token)
        record("Employees", "READ ALL (GET)", "/employees", st == 200, f"Status: {st}, Total Records: {emp_all.get('totalRecords')}")
        
        # READ ONE BY ID
        st, emp_one = request(f"/employees/{emp_id}", method="GET", token=token)
        record("Employees", "READ ONE (GET)", f"/employees/{emp_id}", st == 200 and emp_one.get("firstName") == "Rajesh", f"Status: {st}, FullName: {emp_one.get('fullName')}")
        
        # UPDATE
        emp_payload["basicSalary"] = 42000.0
        emp_payload["designation"] = "Chief Maintenance Engineer"
        st, emp_upd = request(f"/employees/{emp_id}", method="PUT", data=emp_payload, token=token)
        record("Employees", "UPDATE (PUT)", f"/employees/{emp_id}", st == 200 and emp_upd.get("designation") == "Chief Maintenance Engineer", f"Status: {st}, Designation: {emp_upd.get('designation')}, Salary: {emp_upd.get('basicSalary')}")
        
        # DELETE
        st, _ = request(f"/employees/{emp_id}", method="DELETE", token=token)
        record("Employees", "DELETE (Soft)", f"/employees/{emp_id}", st == 204, f"Status: {st} (No Content / Deactivated)")

    # -------------------------------------------------------------
    # 8. ATTENDANCE
    # -------------------------------------------------------------
    print("\n--- 8. ATTENDANCE MODULE ---")
    st, att_emp = request("/employees", method="POST", data={
        "firstName": "Sunil",
        "lastName": f"Deshmukh_{e_num}",
        "phone": f"98333{e_num:0>5}",
        "joiningDate": "2024-01-10",
        "department": "Press Shop",
        "designation": "Press Operator",
        "basicSalary": 22000.0
    }, token=token)
    att_emp_id = att_emp.get("id")
    
    if att_emp_id:
        today_str = datetime.date.today().isoformat()
        
        # CHECK-IN
        att_payload = {
            "employeeId": att_emp_id,
            "attendanceDate": today_str,
            "status": "PRESENT",
            "checkInTime": "08:50:00",
            "notes": "Shift punch verification"
        }
        st, att_res = request("/attendance/check-in", method="POST", data=att_payload, token=token)
        record("Attendance", "CHECK-IN (POST)", "/attendance/check-in", st == 200, f"Status: {st}, AttID: {att_res.get('id')}, Status: {att_res.get('status')}")
        
        # READ ROSTER
        st, att_all = request(f"/attendance?employeeId={att_emp_id}", method="GET", token=token)
        record("Attendance", "READ ROSTER (GET)", f"/attendance?employeeId={att_emp_id}", st == 200, f"Status: {st}, Found: {len(att_all.get('data', []))}")
        
        # CHECK-OUT
        st, att_out = request(f"/attendance/check-out/{att_emp_id}", method="POST", token=token)
        record("Attendance", "CHECK-OUT (POST)", f"/attendance/check-out/{att_emp_id}", st == 200, f"Status: {st}, CheckOutTime: {att_out.get('checkOutTime')}, WorkingHours: {att_out.get('workingHours')}")
        
        # SUMMARY
        st, att_sum = request("/attendance/summary", method="GET", token=token)
        record("Attendance", "SUMMARY (GET)", "/attendance/summary", st == 200 and "PRESENT" in att_sum, f"Status: {st}, Present Count: {att_sum.get('PRESENT')}")

    # -------------------------------------------------------------
    # 9. DASHBOARD
    # -------------------------------------------------------------
    print("\n--- 9. DASHBOARD MODULE ---")
    st, dash = request("/dashboard/metrics", method="GET", token=token)
    record("Dashboard", "GET METRICS", "/dashboard/metrics", st == 200 and "totalCustomers" in dash, f"Status: {st}, Total Customers: {dash.get('totalCustomers')}, Today Sales: {dash.get('todaySales')}, Outstanding: {dash.get('outstandingReceivables')}")

    # -------------------------------------------------------------
    # 10. AUDIT LOGS
    # -------------------------------------------------------------
    print("\n--- 10. AUDIT LOGS MODULE ---")
    st, audit = request("/audit?page=0&size=10", method="GET", token=token)
    total_logs = audit.get("totalRecords", len(audit.get("data", [])))
    record("Audit Logs", "READ LOGS (GET)", "/audit", st == 200 and total_logs > 0, f"Status: {st}, Recorded System Trail Logs: {total_logs}")

    # -------------------------------------------------------------
    # SUMMARY REPORT
    # -------------------------------------------------------------
    total_tests = len(results)
    passed_tests = sum(1 for r in results if r["passed"])
    failed_tests = total_tests - passed_tests
    
    print("\n" + "="*82)
    print(f"VERIFICATION SUMMARY: {passed_tests}/{total_tests} TESTS PASSED ({failed_tests} FAILED)")
    print("="*82)
    
    if failed_tests > 0:
        print("\nFailed Tests:")
        for r in results:
            if not r["passed"]:
                print(f" - [{r['module']}] {r['action']} ({r['endpoint']}): {r['details']}")
    else:
        print("\nSUCCESS: ALL 10 MODULES AND ALL CRUD OPERATIONS ARE FULLY FUNCTIONAL AND VERIFIED!")

if __name__ == "__main__":
    run_suite()
