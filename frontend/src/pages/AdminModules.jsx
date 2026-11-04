import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiRequest } from '../services/api.js';

function AdminBanner({ title, subtitle }) {
  const navigate = useNavigate();
  return (
    <header className="resort-header">
      <div>
        <h1>{title}</h1>
        <div className="subtitle">{subtitle}</div>
      </div>
      <div className="header-actions no-print">
        <button className="btn btn-gray" onClick={() => navigate('/')}>
          ← DASHBOARD
        </button>
      </div>
    </header>
  );
}

/* =========================================================
 * 1. ManageCashiersPage (Mirrors ManageCashiersFrame.java)
 * ========================================================= */
export function ManageCashiersPage() {
  const [cashiers, setCashiers] = useState([]);
  const [selected, setSelected] = useState(null);
  const [username, setUsername] = useState('');
  const [fullName, setFullName] = useState('');
  const [password, setPassword] = useState('');
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const res = await apiRequest('/cashiers');
      setCashiers(res.data || []);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleAdd = async (e) => {
    e.preventDefault();
    setError('');
    setMsg('');
    try {
      await apiRequest('/cashiers', {
        method: 'POST',
        body: { username, full_name: fullName, password },
      });
      setMsg('Cashier added successfully.');
      setUsername('');
      setFullName('');
      setPassword('');
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleToggle = async () => {
    if (!selected) {
      setError('Please select a cashier from the table first.');
      return;
    }
    const next = selected.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    if (!window.confirm(`Change cashier status to ${next}?`)) return;
    setError('');
    setMsg('');
    try {
      await apiRequest(`/cashiers/${selected.user_id}/status`, {
        method: 'PATCH',
        body: { status: next },
      });
      setMsg(`Cashier status changed to ${next}.`);
      setSelected({ ...selected, status: next });
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleResetPassword = async () => {
    if (!selected) {
      setError('Please select a cashier from the table first.');
      return;
    }
    const newPass = window.prompt('New password (6 or more characters):');
    if (newPass === null) return;
    setError('');
    setMsg('');
    try {
      await apiRequest(`/cashiers/${selected.user_id}/password`, {
        method: 'PATCH',
        body: { password: newPass },
      });
      setMsg('Password reset successfully.');
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="page-shell">
      <AdminBanner title="MANAGE CASHIERS" subtitle="Manage cashier accounts and access" />
      <main className="page-content">
        {error && <div className="alert alert-error">{error}</div>}
        {msg && <div className="alert alert-success">{msg}</div>}

        <div className="split-layout">
          <div className="card">
            <h3 style={{ fontSize: 18 }}>Cashier Accounts</h3>
            <p style={{ fontSize: 12, color: '#5b7780', margin: '4px 0 12px' }}>
              Select a cashier to manage their account.
            </p>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Username</th>
                    <th>Full Name</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {cashiers.map((u) => (
                    <tr
                      key={u.user_id}
                      className={selected?.user_id === u.user_id ? 'selected' : ''}
                      onClick={() => setSelected(u)}
                    >
                      <td>{u.user_id}</td>
                      <td>{u.username}</td>
                      <td>{u.full_name}</td>
                      <td className={u.status === 'ACTIVE' ? 'status-free' : 'status-booked'}>
                        {u.status}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <form className="card" onSubmit={handleAdd}>
            <h3 style={{ fontSize: 19 }}>Cashier Details</h3>
            <p style={{ fontSize: 12, color: '#5b7780', margin: '4px 0 14px' }}>
              Enter the new cashier information below.
            </p>
            <div className="form-group">
              <label>Username (4-20, letters/numbers/_)</label>
              <input className="input" value={username} onChange={(e) => setUsername(e.target.value)} />
            </div>
            <div className="form-group">
              <label>Full Name</label>
              <input className="input" value={fullName} onChange={(e) => setFullName(e.target.value)} />
            </div>
            <div className="form-group">
              <label>Password (6 or more chars)</label>
              <input
                type="password"
                className="input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>

            <div style={{ display: 'grid', gap: 10, marginTop: 18 }}>
              <button type="submit" className="btn btn-teal btn-block">
                ADD CASHIER
              </button>
              <button type="button" className="btn btn-blue btn-block" onClick={handleToggle}>
                ENABLE / DISABLE
              </button>
              <button type="button" className="btn btn-danger btn-block" onClick={handleResetPassword}>
                RESET PASSWORD
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}

/* =========================================================
 * 2. ManageCustomersPage (Mirrors ManageCustomersFrame.java)
 * ========================================================= */
export function ManageCustomersPage() {
  const [customers, setCustomers] = useState([]);
  const [selectedId, setSelectedId] = useState(0);
  const [name, setName] = useState('');
  const [contact, setContact] = useState('');
  const [email, setEmail] = useState('');
  const [address, setAddress] = useState('');
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const res = await apiRequest('/customers');
      setCustomers(res.data || []);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const selectRow = (c) => {
    setSelectedId(c.customer_id);
    setName(c.full_name || '');
    setContact(c.contact_number || '');
    setEmail(c.email || '');
    setAddress(c.address || '');
  };

  const clearForm = () => {
    setSelectedId(0);
    setName('');
    setContact('');
    setEmail('');
    setAddress('');
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    if (!selectedId) {
      setError('Please select a customer from the table first.');
      return;
    }
    setError('');
    setMsg('');
    try {
      await apiRequest(`/customers/${selectedId}`, {
        method: 'PUT',
        body: { full_name: name, contact_number: contact, email, address },
      });
      setMsg('Customer updated successfully.');
      clearForm();
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="page-shell">
      <AdminBanner title="MANAGE CUSTOMERS" subtitle="View and update customer information" />
      <main className="page-content">
        {error && <div className="alert alert-error">{error}</div>}
        {msg && <div className="alert alert-success">{msg}</div>}

        <div className="split-layout">
          <div className="card">
            <h3 style={{ fontSize: 18 }}>Customer Records</h3>
            <p style={{ fontSize: 12, color: '#5b7780', margin: '4px 0 12px' }}>
              Select a customer to view or edit their information.
            </p>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Contact</th>
                    <th>Email</th>
                    <th>Address</th>
                    <th>Has Account</th>
                  </tr>
                </thead>
                <tbody>
                  {customers.map((c) => (
                    <tr
                      key={c.customer_id}
                      className={selectedId === c.customer_id ? 'selected' : ''}
                      onClick={() => selectRow(c)}
                    >
                      <td>{c.customer_id}</td>
                      <td>{c.full_name}</td>
                      <td>{c.contact_number || ''}</td>
                      <td>{c.email || ''}</td>
                      <td>{c.address || ''}</td>
                      <td>{c.user_id ? 'Yes' : 'No'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <form className="card" onSubmit={handleUpdate}>
            <h3 style={{ fontSize: 19 }}>Customer Details</h3>
            <p style={{ fontSize: 12, color: '#5b7780', margin: '4px 0 14px' }}>
              Update the selected customer&apos;s details.
            </p>
            <div className="form-group">
              <label>Full Name</label>
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

            <div style={{ display: 'grid', gap: 9, marginTop: 18 }}>
              <button type="submit" className="btn btn-blue btn-block">
                UPDATE CUSTOMER
              </button>
              <button type="button" className="btn btn-gray btn-block" onClick={clearForm}>
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
 * 3. ManageCottagesPage (Mirrors ManageCottagesFrame.java)
 * ========================================================= */
export function ManageCottagesPage() {
  const [cottages, setCottages] = useState([]);
  const [selectedId, setSelectedId] = useState(0);
  const [name, setName] = useState('');
  const [type, setType] = useState('');
  const [capacity, setCapacity] = useState('');
  const [price, setPrice] = useState('');
  const [status, setStatus] = useState('AVAILABLE');
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const res = await apiRequest('/cottages');
      setCottages(res.data || []);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const selectRow = (c) => {
    setSelectedId(c.cottage_id);
    setName(c.cottage_name);
    setType(c.cottage_type);
    setCapacity(String(c.capacity));
    setPrice(String(c.rental_price));
    setStatus(c.status);
  };

  const clearForm = () => {
    setSelectedId(0);
    setName('');
    setType('');
    setCapacity('');
    setPrice('');
    setStatus('AVAILABLE');
  };

  const handleAdd = async () => {
    setError('');
    setMsg('');
    try {
      await apiRequest('/cottages', {
        method: 'POST',
        body: { cottage_name: name, cottage_type: type, capacity, rental_price: price, status },
      });
      setMsg('Cottage added successfully.');
      clearForm();
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleUpdate = async () => {
    if (!selectedId) {
      setError('Please select a cottage from the table first.');
      return;
    }
    setError('');
    setMsg('');
    try {
      await apiRequest(`/cottages/${selectedId}`, {
        method: 'PUT',
        body: { cottage_name: name, cottage_type: type, capacity, rental_price: price, status },
      });
      setMsg('Cottage updated successfully.');
      clearForm();
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDelete = async () => {
    if (!selectedId) {
      setError('Please select a cottage from the table first.');
      return;
    }
    if (!window.confirm('Are you sure you want to delete this cottage?')) return;
    setError('');
    setMsg('');
    try {
      await apiRequest(`/cottages/${selectedId}`, { method: 'DELETE' });
      setMsg('Cottage deleted successfully.');
      clearForm();
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="page-shell">
      <AdminBanner
        title="MANAGE COTTAGES"
        subtitle="Manage cottage details, rental prices, capacity, and status"
      />
      <main className="page-content">
        {error && <div className="alert alert-error">{error}</div>}
        {msg && <div className="alert alert-success">{msg}</div>}

        <div className="split-layout">
          <div className="card">
            <h3 style={{ fontSize: 18 }}>Cottage Records</h3>
            <p style={{ fontSize: 12, color: '#5b7780', margin: '4px 0 12px' }}>
              Select a cottage to view or edit its details.
            </p>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Capacity</th>
                    <th>Price</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {cottages.map((c) => (
                    <tr
                      key={c.cottage_id}
                      className={selectedId === c.cottage_id ? 'selected' : ''}
                      onClick={() => selectRow(c)}
                    >
                      <td>{c.cottage_id}</td>
                      <td>{c.cottage_name}</td>
                      <td>{c.cottage_type}</td>
                      <td>{c.capacity}</td>
                      <td>₱{Number(c.rental_price).toFixed(2)}</td>
                      <td>{c.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="card">
            <h3 style={{ fontSize: 19 }}>Cottage Details</h3>
            <p style={{ fontSize: 12, color: '#5b7780', margin: '4px 0 14px' }}>
              Enter the cottage information below.
            </p>
            <div className="form-group">
              <label>Cottage Name</label>
              <input className="input" value={name} onChange={(e) => setName(e.target.value)} />
            </div>
            <div className="form-group">
              <label>Type (e.g., Small Cottage)</label>
              <input className="input" value={type} onChange={(e) => setType(e.target.value)} />
            </div>
            <div className="form-group">
              <label>Capacity (number of persons)</label>
              <input className="input" value={capacity} onChange={(e) => setCapacity(e.target.value)} />
            </div>
            <div className="form-group">
              <label>Rental Price (PHP)</label>
              <input className="input" value={price} onChange={(e) => setPrice(e.target.value)} />
            </div>
            <div className="form-group">
              <label>Status</label>
              <select className="select" value={status} onChange={(e) => setStatus(e.target.value)}>
                <option value="AVAILABLE">AVAILABLE</option>
                <option value="UNAVAILABLE">UNAVAILABLE</option>
                <option value="MAINTENANCE">MAINTENANCE</option>
              </select>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginTop: 16 }}>
              <button className="btn btn-teal" onClick={handleAdd}>
                ADD COTTAGE
              </button>
              <button className="btn btn-blue" onClick={handleUpdate}>
                UPDATE COTTAGE
              </button>
              <button className="btn btn-danger" onClick={handleDelete}>
                DELETE COTTAGE
              </button>
              <button className="btn btn-gray" onClick={clearForm}>
                CLEAR FORM
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

/* =========================================================
 * 4. ManageRoomsPage (Mirrors ManageRoomsFrame.java)
 * ========================================================= */
export function ManageRoomsPage() {
  const [rooms, setRooms] = useState([]);
  const [selectedId, setSelectedId] = useState(0);
  const [name, setName] = useState('');
  const [type, setType] = useState('');
  const [capacity, setCapacity] = useState('');
  const [price, setPrice] = useState('');
  const [status, setStatus] = useState('AVAILABLE');
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const res = await apiRequest('/rooms');
      setRooms(res.data || []);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const selectRow = (r) => {
    setSelectedId(r.room_id);
    setName(r.room_name);
    setType(r.room_type);
    setCapacity(String(r.max_capacity));
    setPrice(String(r.price_per_night));
    setStatus(r.status);
  };

  const clearForm = () => {
    setSelectedId(0);
    setName('');
    setType('');
    setCapacity('');
    setPrice('');
    setStatus('AVAILABLE');
  };

  const handleAdd = async () => {
    setError('');
    setMsg('');
    try {
      await apiRequest('/rooms', {
        method: 'POST',
        body: { room_name: name, room_type: type, max_capacity: capacity, price_per_night: price, status },
      });
      setMsg('Room added successfully.');
      clearForm();
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleUpdate = async () => {
    if (!selectedId) {
      setError('Please select a room from the table first.');
      return;
    }
    setError('');
    setMsg('');
    try {
      await apiRequest(`/rooms/${selectedId}`, {
        method: 'PUT',
        body: { room_name: name, room_type: type, max_capacity: capacity, price_per_night: price, status },
      });
      setMsg('Room updated successfully.');
      clearForm();
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDelete = async () => {
    if (!selectedId) {
      setError('Please select a room from the table first.');
      return;
    }
    if (!window.confirm('Are you sure you want to delete this room?')) return;
    setError('');
    setMsg('');
    try {
      await apiRequest(`/rooms/${selectedId}`, { method: 'DELETE' });
      setMsg('Room deleted successfully.');
      clearForm();
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="page-shell">
      <AdminBanner
        title="MANAGE ROOMS"
        subtitle="Manage room details, capacity, nightly prices, and availability."
      />
      <main className="page-content">
        {error && <div className="alert alert-error">{error}</div>}
        {msg && <div className="alert alert-success">{msg}</div>}

        <div className="split-layout">
          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
              <div>
                <h3 style={{ fontSize: 18 }}>Room Records</h3>
                <div style={{ fontSize: 12, color: '#5b7780' }}>
                  Select a room to view or edit its information.
                </div>
              </div>
              <button className="btn btn-blue" onClick={load}>
                REFRESH
              </button>
            </div>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Room Name</th>
                    <th>Type</th>
                    <th>Max Capacity</th>
                    <th>Price / Night</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {rooms.map((r) => (
                    <tr
                      key={r.room_id}
                      className={selectedId === r.room_id ? 'selected' : ''}
                      onClick={() => selectRow(r)}
                    >
                      <td>{r.room_id}</td>
                      <td>{r.room_name}</td>
                      <td>{r.room_type}</td>
                      <td>{r.max_capacity}</td>
                      <td>₱{Number(r.price_per_night).toFixed(2)}</td>
                      <td>{r.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="card">
            <h3 style={{ fontSize: 19 }}>Room Information</h3>
            <p style={{ fontSize: 12, color: '#5b7780', margin: '4px 0 14px' }}>
              Enter the room details below.
            </p>
            <div className="form-group">
              <label>Room Name</label>
              <input className="input" value={name} onChange={(e) => setName(e.target.value)} />
            </div>
            <div className="form-group">
              <label>Room Type (e.g. Standard Room)</label>
              <input className="input" value={type} onChange={(e) => setType(e.target.value)} />
            </div>
            <div className="form-group">
              <label>Maximum Capacity</label>
              <input className="input" value={capacity} onChange={(e) => setCapacity(e.target.value)} />
            </div>
            <div className="form-group">
              <label>Price per Night (₱)</label>
              <input className="input" value={price} onChange={(e) => setPrice(e.target.value)} />
            </div>
            <div className="form-group">
              <label>Room Status</label>
              <select className="select" value={status} onChange={(e) => setStatus(e.target.value)}>
                <option value="AVAILABLE">AVAILABLE</option>
                <option value="UNAVAILABLE">UNAVAILABLE</option>
                <option value="MAINTENANCE">MAINTENANCE</option>
              </select>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginTop: 16 }}>
              <button className="btn btn-teal" onClick={handleAdd}>
                ADD ROOM
              </button>
              <button className="btn btn-blue" onClick={handleUpdate}>
                UPDATE ROOM
              </button>
              <button className="btn btn-danger" onClick={handleDelete}>
                DELETE ROOM
              </button>
              <button className="btn btn-gray" onClick={clearForm}>
                CLEAR FORM
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

/* =========================================================
 * 5. ManageReservationsPage (Mirrors ManageReservationsFrame.java)
 * ========================================================= */
export function ManageReservationsPage() {
  const [rows, setRows] = useState([]);
  const [selectedId, setSelectedId] = useState(0);
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const res = await apiRequest('/reservations');
      setRows(res.data || []);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const changeStatus = async (newStatus) => {
    if (!selectedId) {
      setError('Please select a reservation from the table first.');
      return;
    }
    const promptMsg =
      newStatus === 'CANCELLED'
        ? `Cancel reservation #${selectedId}?\n\nThe cottage or room will become available again.\nRefunds will be handled manually by the cashier.`
        : `Change reservation #${selectedId} status to ${newStatus.replace('_', ' ')}?`;
    if (!window.confirm(promptMsg)) return;

    setError('');
    setMsg('');
    try {
      const res = await apiRequest(`/reservations/${selectedId}/status`, {
        method: 'PATCH',
        body: { status: newStatus },
      });
      setMsg(res.message);
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="page-shell">
      <AdminBanner
        title="MANAGE RESERVATIONS"
        subtitle="Review bookings and manage guest reservation status"
      />
      <main className="page-content">
        {error && <div className="alert alert-error">{error}</div>}
        {msg && <div className="alert alert-success">{msg}</div>}

        <div className="card">
          <h3 style={{ fontSize: 18 }}>Reservation Records</h3>
          <p style={{ fontSize: 12, color: '#5b7780', margin: '4px 0 12px' }}>
            Select a reservation before changing its status.
          </p>

          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Customer</th>
                  <th>Cottage/Room</th>
                  <th>Start</th>
                  <th>End</th>
                  <th>Total</th>
                  <th>Paid</th>
                  <th>Status</th>
                  <th>Payment</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((r) => (
                  <tr
                    key={r.reservation_id}
                    className={selectedId === r.reservation_id ? 'selected' : ''}
                    onClick={() => setSelectedId(r.reservation_id)}
                  >
                    <td>{r.reservation_id}</td>
                    <td>{r.customer_name}</td>
                    <td>{r.items || '-'}</td>
                    <td>{r.start_dt || ''}</td>
                    <td>{r.end_dt || ''}</td>
                    <td>₱{Number(r.total_amount).toFixed(2)}</td>
                    <td>₱{Number(r.amount_paid).toFixed(2)}</td>
                    <td><strong>{r.reservation_status}</strong></td>
                    <td>{r.payment_status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, marginTop: 16 }}>
            <button className="btn btn-gray" onClick={load}>
              REFRESH
            </button>
            <button className="btn btn-blue" onClick={() => changeStatus('CHECKED_IN')}>
              CHECK-IN
            </button>
            <button className="btn btn-green" onClick={() => changeStatus('COMPLETED')}>
              COMPLETE
            </button>
            <button className="btn btn-danger" onClick={() => changeStatus('CANCELLED')}>
              CANCEL RESERVATION
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}

/* =========================================================
 * 7. SalesReportPage (Mirrors SalesReportFrame.java)
 * ========================================================= */
export function SalesReportPage() {
  const today = new Date().toISOString().slice(0, 10);
  const [period, setPeriod] = useState('Daily');
  const [date, setDate] = useState(today);
  const [report, setReport] = useState(null);
  const [error, setError] = useState('');

  const generate = async () => {
    setError('');
    try {
      const res = await apiRequest(
        `/reports/sales?period=${encodeURIComponent(period)}&date=${encodeURIComponent(date)}`
      );
      setReport(res.data);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    generate();
  }, []);

  return (
    <div className="page-shell">
      <AdminBanner
        title="SALES REPORTS"
        subtitle="View resort sales, transaction counts, and revenue summaries."
      />
      <main className="page-content">
        {error && <div className="alert alert-error">{error}</div>}

        <div
          className="card no-print"
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'flex-end',
            flexWrap: 'wrap',
            gap: 12,
            marginBottom: 16,
          }}
        >
          <div style={{ display: 'flex', gap: 14, flexWrap: 'wrap' }}>
            <div>
              <label style={{ display: 'block', fontSize: 12, fontWeight: 700, marginBottom: 5 }}>
                Report Period
              </label>
              <select
                className="select"
                style={{ width: 150 }}
                value={period}
                onChange={(e) => setPeriod(e.target.value)}
              >
                <option value="Daily">Daily</option>
                <option value="Weekly">Weekly</option>
                <option value="Monthly">Monthly</option>
              </select>
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 12, fontWeight: 700, marginBottom: 5 }}>
                Reference Date (yyyy-MM-dd)
              </label>
              <input
                type="date"
                className="input"
                style={{ width: 180 }}
                value={date}
                onChange={(e) => setDate(e.target.value)}
              />
            </div>
          </div>

          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn btn-green" onClick={generate}>
              GENERATE REPORT
            </button>
            <button className="btn btn-blue" onClick={() => window.print()}>
              PRINT REPORT
            </button>
          </div>
        </div>

        <div className="card printable-area">
          <h3 style={{ fontSize: 18 }}>Sales Breakdown</h3>
          <p style={{ fontSize: 12, color: '#5b7780', margin: '4px 0 12px' }}>
            Sales are grouped by date within the selected period.
          </p>

          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th style={{ textAlign: 'right' }}>Transactions</th>
                  <th style={{ textAlign: 'right' }}>Cottage</th>
                  <th style={{ textAlign: 'right' }}>Room</th>
                  <th style={{ textAlign: 'right' }}>Total</th>
                </tr>
              </thead>
              <tbody>
                {report?.rows?.map((r) => (
                  <tr key={r.date}>
                    <td>{r.date}</td>
                    <td style={{ textAlign: 'right' }}>{r.transactions}</td>
                    <td style={{ textAlign: 'right' }}>₱{r.cottage}</td>
                    <td style={{ textAlign: 'right' }}>₱{r.room}</td>
                    <td style={{ textAlign: 'right' }}><strong>₱{r.total}</strong></td>
                  </tr>
                ))}
                {report?.rows?.length > 0 && (
                  <tr style={{ background: '#eaf7f7', fontWeight: 700 }}>
                    <td>TOTAL</td>
                    <td style={{ textAlign: 'right' }}>{report.totals.transactions}</td>
                    <td style={{ textAlign: 'right' }}>₱{report.totals.cottage}</td>
                    <td style={{ textAlign: 'right' }}>₱{report.totals.room}</td>
                    <td style={{ textAlign: 'right' }}>₱{report.totals.grand_total}</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          {report && (
            <div style={{ marginTop: 14, fontWeight: 700, fontSize: 14 }}>
              {report.rows.length > 0
                ? `${report.period} Report: ${report.from} to ${report.to}  |  Transactions: ${report.totals.transactions}  |  Total Sales: ₱${report.totals.grand_total}`
                : `${report.period} Report: ${report.from} to ${report.to}  |  No sales records found.`}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
