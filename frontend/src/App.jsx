import React, { useEffect, useState } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { apiRequest, setCsrfToken } from './services/api.js';
import LoginPage from './pages/LoginPage.jsx';
import { AdminDashboard, CashierDashboard } from './pages/Dashboards.jsx';
import POSPage from './pages/POSPage.jsx';
import {
  AvailabilityPage,
  BookingListPage,
  CustomerRegistrationPage,
  TransactionHistoryPage,
} from './pages/SharedModules.jsx';
import {
  ManageCashiersPage,
  ManageCustomersPage,
  ManageCottagesPage,
  ManageRoomsPage,
  ManageReservationsPage,
  SalesReportPage,
} from './pages/AdminModules.jsx';

export default function App() {
  const [user, setUser] = useState(null);
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    apiRequest('/auth/me')
      .then((res) => {
        if (res.data?.user) {
          setUser(res.data.user);
          setCsrfToken(res.data.csrf_token);
        }
      })
      .catch(() => {})
      .finally(() => setChecking(false));
  }, []);

  const handleLogout = async () => {
    if (!window.confirm('Are you sure you want to log out of Promise Resort?')) return;
    try {
      await apiRequest('/auth/logout', { method: 'POST' });
    } catch {
      // ignore
    }
    setCsrfToken('');
    setUser(null);
  };

  if (checking) {
    return <div style={{ padding: 40, textAlign: 'center' }}>Loading Promise Resort POS...</div>;
  }

  if (!user) {
    return <LoginPage onLogin={setUser} />;
  }

  const isAdmin = user.role === 'ADMIN';

  return (
    <Routes>
      <Route
        path="/"
        element={
          isAdmin ? (
            <AdminDashboard user={user} onLogout={handleLogout} />
          ) : (
            <CashierDashboard user={user} onLogout={handleLogout} />
          )
        }
      />

      {/* Cashier + Admin Shared POS & Operational Routes */}
      <Route path="/pos" element={<POSPage />} />
      <Route path="/availability" element={<AvailabilityPage />} />
      <Route path="/bookings/:type" element={<BookingListPage />} />
      <Route path="/customers/register" element={<CustomerRegistrationPage />} />
      <Route path="/transactions" element={<TransactionHistoryPage />} />

      {/* Admin-Only Management Routes */}
      {isAdmin && (
        <>
          <Route path="/admin/cashiers" element={<ManageCashiersPage />} />
          <Route path="/admin/customers" element={<ManageCustomersPage />} />
          <Route path="/admin/cottages" element={<ManageCottagesPage />} />
          <Route path="/admin/rooms" element={<ManageRoomsPage />} />
          <Route path="/admin/reservations" element={<ManageReservationsPage />} />
          <Route path="/admin/sales-reports" element={<SalesReportPage />} />
        </>
      )}

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
