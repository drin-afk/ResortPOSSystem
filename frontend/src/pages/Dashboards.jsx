import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const ADMIN_CARDS = [
  { name: 'Manage Cashiers', desc: 'Manage staff accounts', icon: '♟', bg: '#e8f6fa', accent: '#0089a6', path: '/admin/cashiers' },
  { name: 'Manage Customers', desc: 'View customer records', icon: '☺', bg: '#fff4e6', accent: '#eb9132', path: '/admin/customers' },
  { name: 'Manage Cottages', desc: 'Manage cottage details', icon: '⌂', bg: '#e1f4fa', accent: '#006b91', path: '/admin/cottages' },
  { name: 'Manage Rooms', desc: 'Manage room details', icon: '▣', bg: '#e1f6ef', accent: '#14a07d', path: '/admin/rooms' },
  { name: 'Manage Reservations', desc: 'Manage resort bookings', icon: '▤', bg: '#fff7de', accent: '#dcaf32', path: '/admin/reservations' },
  { name: 'Sales Reports', desc: 'View resort sales', icon: '▦', bg: '#e4f1fa', accent: '#0077aa', path: '/admin/sales-reports' },
  { name: 'Transaction History', desc: 'Review past transactions', icon: '↻', bg: '#ddf6f1', accent: '#18a096', path: '/transactions' },
  { name: 'Availability', desc: 'Check available facilities', icon: '✓', bg: '#e8f5fa', accent: '#006b91', path: '/availability' },
];

export function AdminDashboard({ user, onLogout }) {
  const [query, setQuery] = useState('');
  const navigate = useNavigate();

  const filtered = ADMIN_CARDS.filter((c) =>
    c.name.toLowerCase().includes(query.trim().toLowerCase())
  );

  return (
    <div className="page-shell">
      <header className="resort-header" style={{ background: 'linear-gradient(135deg, #009eaa, #006b91)' }}>
        <div>
          <h1>PROMISE RESORT</h1>
          <div style={{ fontSize: 15, marginTop: 4 }}>Welcome back, {user.full_name}!</div>
          <div className="subtitle">Resort Management Dashboard</div>
        </div>
        <div className="header-actions">
          <button className="btn btn-teal" onClick={() => navigate('/pos')}>
            OPEN POS
          </button>
          <button className="btn btn-danger" onClick={onLogout}>
            ↪ LOG OUT
          </button>
        </div>
      </header>

      <main className="page-content">
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            background: '#fff',
            border: '1px solid #d2e7ea',
            borderRadius: 6,
            padding: '4px 12px',
            marginBottom: 18,
          }}
        >
          <span style={{ fontSize: 20, color: '#009eaa', marginRight: 10 }}>⌕</span>
          <input
            className="input"
            style={{ border: 'none', boxShadow: 'none' }}
            placeholder="Search dashboard features..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </div>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2 style={{ fontSize: 19 }}>Quick Access</h2>
          <button
            onClick={() => setQuery('')}
            style={{ background: 'none', border: 'none', color: '#006b91', cursor: 'pointer', fontSize: 12 }}
          >
            View All
          </button>
        </div>

        <div className="dashboard-grid-3">
          {filtered.map((c) => (
            <div
              key={c.name}
              onClick={() => navigate(c.path)}
              style={{
                background: c.bg,
                borderRadius: 8,
                padding: 18,
                cursor: 'pointer',
                border: '2px solid transparent',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
                minHeight: 150,
                transition: 'border-color 0.15s ease',
              }}
              onMouseEnter={(e) => (e.currentTarget.style.borderColor = c.accent)}
              onMouseLeave={(e) => (e.currentTarget.style.borderColor = 'transparent')}
            >
              <div
                style={{
                  width: 39,
                  height: 39,
                  background: c.accent,
                  color: '#fff',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontWeight: 700,
                  fontSize: 18,
                  borderRadius: 6,
                }}
              >
                {c.icon}
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 18 }}>
                <div>
                  <div style={{ fontWeight: 700, fontSize: 14 }}>{c.name}</div>
                  <div style={{ fontSize: 12, color: '#5a737d', marginTop: 4 }}>{c.desc}</div>
                </div>
                <span style={{ fontSize: 22, color: '#698c96' }}>›</span>
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}

const CASHIER_CARDS = [
  { no: '01', title: 'New Transaction', desc: 'Process customer payments and purchases', btn: 'OPEN POS', accent: '#006b91', path: '/pos' },
  { no: '02', title: 'Register Customer', desc: 'Add customer information to the system', btn: 'REGISTER', accent: '#00796b', path: '/customers/register' },
  { no: '03', title: 'Check Availability', desc: 'Check available cottages and rooms', btn: 'CHECK NOW', accent: '#009eaa', path: '/availability' },
  { no: '04', title: 'Room Reservations', desc: 'View and monitor room bookings', btn: 'VIEW ROOMS', accent: '#006b91', path: '/bookings/ROOM' },
  { no: '05', title: 'Cottage Rentals', desc: 'View and monitor cottage bookings', btn: 'VIEW COTTAGES', accent: '#00796b', path: '/bookings/COTTAGE' },
  { no: '06', title: 'Transaction History', desc: 'Review previous resort transactions', btn: 'VIEW HISTORY', accent: '#009eaa', path: '/transactions' },
];

export function CashierDashboard({ user, onLogout }) {
  const navigate = useNavigate();

  return (
    <div className="page-shell">
      <header className="resort-header">
        <div>
          <div className="brand">PROMISE RESORT</div>
          <h1>CASHIER DASHBOARD</h1>
          <div className="subtitle">Logged in as: {user.full_name}</div>
        </div>
        <div className="header-actions">
          <button className="btn btn-danger" onClick={onLogout}>
            LOG OUT
          </button>
        </div>
      </header>

      <main className="page-content">
        <h2 style={{ fontSize: 22 }}>Welcome to Promise Resort!</h2>
        <p style={{ fontSize: 13, color: '#5b7780', marginTop: 4 }}>
          Select a module below to manage resort transactions and bookings.
        </p>

        <div className="dashboard-grid-3" style={{ marginTop: 20 }}>
          {CASHIER_CARDS.map((c) => (
            <div
              key={c.no}
              className="card"
              style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between', minHeight: 185 }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span
                  style={{
                    border: `1px solid ${c.accent}`,
                    color: c.accent,
                    fontWeight: 700,
                    fontSize: 12,
                    padding: '4px 9px',
                  }}
                >
                  {c.no}
                </span>
                <span style={{ width: 45, height: 4, background: c.accent }} />
              </div>

              <div style={{ margin: '14px 0' }}>
                <div style={{ fontSize: 16, fontWeight: 700 }}>{c.title}</div>
                <div style={{ fontSize: 12, color: '#5b7780', marginTop: 6 }}>{c.desc}</div>
              </div>

              <button
                className="btn btn-block"
                style={{ background: c.accent }}
                onClick={() => navigate(c.path)}
              >
                {c.btn}
              </button>
            </div>
          ))}
        </div>
      </main>

      <footer className="page-footer">
        <span>PROMISE RESORT | Point of Sale System</span>
        <strong style={{ color: '#00796b' }}>CASHIER PANEL</strong>
      </footer>
    </div>
  );
}
