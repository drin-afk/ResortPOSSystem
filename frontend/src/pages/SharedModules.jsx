import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiRequest } from '../services/api.js';
import ReceiptModal from '../components/ReceiptModal.jsx';

function getDefaultRange() {
  const now = new Date();
  now.setMinutes(0, 0, 0);
  const end = new Date(now.getTime() + 8 * 3600 * 1000);
  const pad = (n) => String(n).padStart(2, '0');
  const fmt = (d) =>
    `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  return { start: fmt(now), end: fmt(end) };
}

/* =========================================================
 * 1. AvailabilityPage (Mirrors AvailabilityFrame.java)
 * ========================================================= */
export function AvailabilityPage() {
  const navigate = useNavigate();
  const def = getDefaultRange();
  const [start, setStart] = useState(def.start);
  const [end, setEnd] = useState(def.end);
  const [data, setData] = useState({ records: [], free: 0, booked: 0, start: def.start, end: def.end });
  const [error, setError] = useState('');

  const checkAvailability = async () => {
    setError('');
    try {
      const res = await apiRequest(
        `/availability?start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}`
      );
      setData(res.data);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    checkAvailability();
  }, []);

  const statusClass = (val) => {
    if (val === 'AVAILABLE' || val === 'FREE') return 'status-free';
    if (val === 'ALREADY BOOKED' || val === 'BOOKED') return 'status-booked';
    return 'status-other';
  };

  return (
    <div className="page-shell">
      <header className="resort-header">
        <div>
          <div className="brand">PROMISE RESORT</div>
          <h1>CHECK AVAILABILITY</h1>
          <div className="subtitle">Check cottage and room availability for your selected schedule</div>
        </div>
        <div className="header-actions">
          <span className="badge-outline">AVAILABILITY</span>
          <button className="btn btn-gray" onClick={() => navigate('/')}>
            ← DASHBOARD
          </button>
        </div>
      </header>

      <main className="page-content">
        {error && <div className="alert alert-error">{error}</div>}

        <div className="card" style={{ display: 'flex', flexWrap: 'wrap', gap: 12, alignItems: 'center', marginBottom: 16 }}>
          <strong>Start Date and Time</strong>
          <input className="input" style={{ width: 190 }} value={start} onChange={(e) => setStart(e.target.value)} />
          <strong>End Date and Time</strong>
          <input className="input" style={{ width: 190 }} value={end} onChange={(e) => setEnd(e.target.value)} />
          <button className="btn btn-green" onClick={checkAvailability}>
            CHECK AVAILABILITY
          </button>
        </div>

        <div className="card">
          <h3 style={{ fontSize: 17 }}>Cottage and Room Records</h3>
          <p style={{ fontSize: 12, color: '#5b7780', margin: '4px 0 12px' }}>
            Format: yyyy-MM-dd HH:mm (example 2026-11-15 09:00). ALREADY BOOKED means it is reserved during the selected period.
          </p>

          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Type</th>
                  <th>Name</th>
                  <th>Category</th>
                  <th>Capacity</th>
                  <th>Price</th>
                  <th>Status</th>
                  <th>Availability</th>
                </tr>
              </thead>
              <tbody>
                {data.records.map((r) => (
                  <tr key={`${r.type}-${r.id}`}>
                    <td>{r.type}</td>
                    <td>{r.name}</td>
                    <td>{r.category}</td>
                    <td>{r.capacity}</td>
                    <td>₱{Number(r.price).toFixed(2)}</td>
                    <td className={statusClass(r.status)}>{r.status}</td>
                    <td className={statusClass(r.availability)}>{r.availability}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div style={{ marginTop: 12, fontSize: 12, fontWeight: 700, color: '#00796b' }}>
            Period: {data.start} to {data.end} &nbsp;|&nbsp; Free: {data.free} &nbsp;|&nbsp; Already booked: {data.booked}
          </div>
        </div>
      </main>
    </div>
  );
}

/* =========================================================
 * 2. BookingListPage (Mirrors BookingListFrame.java)
 * ========================================================= */
export function BookingListPage() {
  const { type = 'ROOM' } = useParams();
  const itemType = type.toUpperCase() === 'COTTAGE' ? 'COTTAGE' : 'ROOM';
  const navigate = useNavigate();

  const [rows, setRows] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [error, setError] = useState('');

  const loadBookings = async () => {
    setError('');
    try {
      const res = await apiRequest(`/reservations/bookings/${itemType}`);
      setRows(res.data || []);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    loadBookings();
  }, [itemType]);

  const filtered = rows.filter((r) => filter === 'ALL' || r.reservation_status === filter);

  return (
    <div className="page-shell">
      <header className="resort-header">
        <div>
          <div className="brand">PROMISE RESORT</div>
          <h1>{itemType === 'ROOM' ? 'ROOM RESERVATIONS' : 'COTTAGE RENTALS'}</h1>
          <div className="subtitle">
            {itemType === 'ROOM' ? 'View and monitor room reservations' : 'View and monitor cottage bookings'}
          </div>
        </div>
        <div className="header-actions">
          <span className="badge-outline">{itemType === 'ROOM' ? 'ROOMS' : 'COTTAGES'}</span>
          <button className="btn btn-gray" onClick={() => navigate('/')}>
            ← DASHBOARD
          </button>
        </div>
      </header>

      <main className="page-content">
        {error && <div className="alert alert-error">{error}</div>}

        <div
          className="card"
          style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <strong>Filter by status:</strong>
            <select
              className="select"
              style={{ width: 170 }}
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
            >
              <option value="ALL">ALL</option>
              <option value="CONFIRMED">CONFIRMED</option>
              <option value="CHECKED_IN">CHECKED_IN</option>
              <option value="COMPLETED">COMPLETED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
            <button className="btn btn-blue" onClick={loadBookings}>
              REFRESH
            </button>
          </div>
          <div style={{ border: '1px solid #cde5e5', padding: '7px 13px', fontWeight: 700, color: '#00796b' }}>
            {filtered.length} {filtered.length === 1 ? 'booking' : 'bookings'}
          </div>
        </div>

        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Res. ID</th>
                <th>Customer</th>
                <th>{itemType === 'ROOM' ? 'Room' : 'Cottage'}</th>
                <th>Start</th>
                <th>End</th>
                <th>Guests</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Payment</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((r, idx) => (
                <tr key={`${r.reservation_id}-${idx}`}>
                  <td>{r.reservation_id}</td>
                  <td>{r.customer_name}</td>
                  <td>{r.item_name}</td>
                  <td>{r.start_datetime}</td>
                  <td>{r.end_datetime}</td>
                  <td>{r.guests}</td>
                  <td>₱{Number(r.subtotal).toFixed(2)}</td>
                  <td>{r.reservation_status}</td>
                  <td>{r.payment_status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
}

/* =========================================================
 * 3. CustomerRegistrationPage (Mirrors CustomerRegistrationFrame.java)
 * ========================================================= */
export function CustomerRegistrationPage() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState([]);
  const [name, setName] = useState('');
  const [contact, setContact] = useState('');
  const [email, setEmail] = useState('');
  const [address, setAddress] = useState('');
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  const loadCustomers = async () => {
    try {
      const res = await apiRequest('/customers');
      setCustomers(res.data || []);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    loadCustomers();
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setError('');
    setMsg('');
    try {
      await apiRequest('/customers', {
        method: 'POST',
        body: { full_name: name, contact_number: contact, email, address },
      });
      setMsg('Customer registered successfully!');
      setName('');
      setContact('');
      setEmail('');
      setAddress('');
      loadCustomers();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="page-shell">
      <header className="resort-header">
        <div>
          <div className="brand">PROMISE RESORT</div>
          <h1>CUSTOMER REGISTRATION</h1>
          <div className="subtitle">Register customers and manage their contact information</div>
        </div>
        <div className="header-actions">
          <span className="badge-outline">CUSTOMER MANAGEMENT</span>
          <button className="btn btn-gray" onClick={() => navigate('/')}>
            ← DASHBOARD
          </button>
        </div>
      </header>

      <main className="page-content">
        {error && <div className="alert alert-error">{error}</div>}
        {msg && <div className="alert alert-success">{msg}</div>}

        <div className="split-layout">
          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
              <div>
                <h3 style={{ fontSize: 17 }}>Registered Customers</h3>
                <div style={{ fontSize: 12, color: '#5b7780' }}>Customer records saved in the system</div>
              </div>
              <button className="btn btn-blue" onClick={loadCustomers}>
                REFRESH
              </button>
            </div>

            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Full Name</th>
                    <th>Contact Number</th>
                    <th>Email</th>
                    <th>Address</th>
                  </tr>
                </thead>
                <tbody>
                  {customers.map((c) => (
                    <tr key={c.customer_id}>
                      <td>{c.customer_id}</td>
                      <td>{c.full_name}</td>
                      <td>{c.contact_number || ''}</td>
                      <td>{c.email || ''}</td>
                      <td>{c.address || ''}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 10, fontSize: 12 }}>
              <span style={{ color: '#5b7780' }}>Customer list</span>
              <strong style={{ color: '#00796b' }}>
                {customers.length} {customers.length === 1 ? 'customer' : 'customers'}
              </strong>
            </div>
          </div>

          <form className="card" onSubmit={handleSave}>
            <h3 style={{ fontSize: 19 }}>Register Customer</h3>
            <p style={{ fontSize: 12, color: '#5b7780', margin: '4px 0 14px' }}>
              Enter the customer&apos;s details below.
            </p>

            <div className="form-group">
              <label>Full Name *</label>
              <input className="input" value={name} onChange={(e) => setName(e.target.value)} />
            </div>
            <div className="form-group">
              <label>Contact Number (09XXXXXXXXX)</label>
              <input className="input" value={contact} onChange={(e) => setContact(e.target.value)} />
            </div>
            <div className="form-group">
              <label>Email Address</label>
              <input className="input" value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <div className="form-group">
              <label>Home Address</label>
              <input className="input" value={address} onChange={(e) => setAddress(e.target.value)} />
            </div>

            <div style={{ fontSize: 11, fontStyle: 'italic', color: '#5b7780', marginBottom: 12 }}>
              * Required field
            </div>

            <div style={{ display: 'grid', gap: 8 }}>
              <button type="submit" className="btn btn-green btn-block">
                SAVE CUSTOMER
              </button>
              <button
                type="button"
                className="btn btn-gray btn-block"
                onClick={() => {
                  setName('');
                  setContact('');
                  setEmail('');
                  setAddress('');
                }}
              >
                CLEAR FORM
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}

/* =========================================================
 * 4. TransactionHistoryPage (Mirrors CashierTransactionHistoryFram.java)
 * ========================================================= */
export function TransactionHistoryPage() {
  const navigate = useNavigate();
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [list, setList] = useState([]);
  const [error, setError] = useState('');
  const [selectedTxNo, setSelectedTxNo] = useState('');

  const loadHistory = async (f = from, t = to) => {
    setError('');
    try {
      const res = await apiRequest(
        `/transactions?from=${encodeURIComponent(f)}&to=${encodeURIComponent(t)}`
      );
      setList(res.data || []);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    loadHistory('', '');
  }, []);

  const totalSales = list.reduce((acc, t) => acc + Number(t.total_amount || 0), 0);

  return (
    <div className="page-shell">
      <header className="resort-header">
        <div>
          <div className="brand">PROMISE RESORT</div>
          <h1>TRANSACTION HISTORY</h1>
          <div className="subtitle">Review sales records and view transaction receipts</div>
        </div>
        <div className="header-actions">
          <span className="badge-outline">SALES RECORDS</span>
          <button className="btn btn-gray" onClick={() => navigate('/')}>
            ← DASHBOARD
          </button>
        </div>
      </header>

      <main className="page-content">
        {error && <div className="alert alert-error">{error}</div>}

        <div className="card" style={{ display: 'flex', flexWrap: 'wrap', gap: 10, alignItems: 'center', marginBottom: 6 }}>
          <strong>From date:</strong>
          <input
            type="date"
            className="input"
            style={{ width: 160 }}
            value={from}
            onChange={(e) => setFrom(e.target.value)}
          />
          <strong>To date:</strong>
          <input
            type="date"
            className="input"
            style={{ width: 160 }}
            value={to}
            onChange={(e) => setTo(e.target.value)}
          />
          <button className="btn btn-blue" onClick={() => loadHistory(from, to)}>
            SEARCH
          </button>
          <button
            className="btn btn-green"
            onClick={() => {
              setFrom('');
              setTo('');
              loadHistory('', '');
            }}
          >
            SHOW ALL
          </button>
        </div>
        <div style={{ fontSize: 12, color: '#5b7780', marginBottom: 14 }}>
          Date format: yyyy-MM-dd (Click or double-click a transaction row to view its receipt)
        </div>

        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Transaction No</th>
                <th>Date</th>
                <th>Customer</th>
                <th>Cashier</th>
                <th>Payment</th>
                <th>Total (PHP)</th>
                <th>Received (PHP)</th>
                <th>Change (PHP)</th>
              </tr>
            </thead>
            <tbody>
              {list.map((t) => (
                <tr
                  key={t.transaction_id}
                  onClick={() => setSelectedTxNo(t.transaction_no)}
                  title="Click to open receipt"
                >
                  <td><strong>{t.transaction_no}</strong></td>
                  <td>{t.formatted_date || t.transaction_date}</td>
                  <td>{t.customer_name}</td>
                  <td>{t.cashier_name}</td>
                  <td>{t.payment_method}{t.reference_no ? ` (${t.reference_no})` : ''}</td>
                  <td>₱{Number(t.total_amount).toFixed(2)}</td>
                  <td>₱{Number(t.amount_received).toFixed(2)}</td>
                  <td>₱{Number(t.change_amount).toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div
          className="card"
          style={{ marginTop: 14, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
        >
          <strong style={{ color: '#00796b', fontSize: 14 }}>
            Transactions: {list.length} &nbsp;|&nbsp; Total Sales: ₱{totalSales.toFixed(2)}
          </strong>
          <span style={{ fontSize: 12, fontStyle: 'italic', color: '#5b7780' }}>
            Click a transaction row to view &amp; print its receipt
          </span>
        </div>
      </main>

      <ReceiptModal transactionNo={selectedTxNo} onClose={() => setSelectedTxNo('')} />
    </div>
  );
}
