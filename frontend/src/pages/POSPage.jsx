import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiRequest } from '../services/api.js';
import ReceiptModal from '../components/ReceiptModal.jsx';

function defaultSchedule() {
  const now = new Date();
  now.setMinutes(0, 0, 0);
  const pad = (n) => String(n).padStart(2, '0');
  const fmt = (d) =>
    `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;

  const cotEnd = new Date(now.getTime() + 8 * 3600 * 1000);
  const checkIn = new Date(now);
  checkIn.setHours(14, 0, 0, 0);
  const checkOut = new Date(now.getTime() + 24 * 3600 * 1000);
  checkOut.setHours(12, 0, 0, 0);

  return {
    cottageStart: fmt(now),
    cottageEnd: fmt(cotEnd),
    checkIn: fmt(checkIn),
    checkOut: fmt(checkOut),
  };
}

export default function POSPage() {
  const navigate = useNavigate();
  const defaults = defaultSchedule();

  const [customers, setCustomers] = useState([]);
  const [cottages, setCottages] = useState([]);
  const [rooms, setRooms] = useState([]);

  const [customerId, setCustomerId] = useState('');

  const [cottageId, setCottageId] = useState('0');
  const [cottageStart, setCottageStart] = useState(defaults.cottageStart);
  const [cottageEnd, setCottageEnd] = useState(defaults.cottageEnd);
  const [cottageGuests, setCottageGuests] = useState('1');

  const [roomId, setRoomId] = useState('0');
  const [checkIn, setCheckIn] = useState(defaults.checkIn);
  const [checkOut, setCheckOut] = useState(defaults.checkOut);
  const [roomGuests, setRoomGuests] = useState('1');

  const [paymentMethod, setPaymentMethod] = useState('Cash');
  const [amountReceived, setAmountReceived] = useState('');
  const [referenceNo, setReferenceNo] = useState('');

  const [summaryText, setSummaryText] = useState('Click CALCULATE TOTAL to view the transaction summary.');
  const [totalAmount, setTotalAmount] = useState(0);
  const [statusMsg, setStatusMsg] = useState('Waiting for calculation');
  const [error, setError] = useState('');
  const [receiptTxNo, setReceiptTxNo] = useState('');

  const loadMasterData = async () => {
    try {
      const [custRes, cotRes, rmRes] = await Promise.all([
        apiRequest('/customers'),
        apiRequest('/cottages'),
        apiRequest('/rooms'),
      ]);
      setCustomers(custRes.data || []);
      if (custRes.data?.length > 0 && !customerId) {
        setCustomerId(String(custRes.data[0].customer_id));
      }
      setCottages((cotRes.data || []).filter((c) => c.status === 'AVAILABLE'));
      setRooms((rmRes.data || []).filter((r) => r.status === 'AVAILABLE'));
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    loadMasterData();
  }, []);

  const buildPayload = () => ({
    customer_id: Number(customerId),
    cottage_id: Number(cottageId),
    cottage_start: cottageStart,
    cottage_end: cottageEnd,
    cottage_guests: cottageGuests,
    room_id: Number(roomId),
    check_in: checkIn,
    check_out: checkOut,
    room_guests: roomGuests,
    payment_method: paymentMethod,
    amount_received: amountReceived,
    reference_no: referenceNo,
  });

  const handleCalculate = async () => {
    setError('');
    try {
      const res = await apiRequest('/pos/calculate', {
        method: 'POST',
        body: buildPayload(),
      });
      const data = res.data;
      let sb = 'PROMISE RESORT\nTRANSACTION SUMMARY\n--------------------------------\n\n';
      for (const item of data.items) {
        sb += `${item.description}\n  ${item.quantity} x ${item.unit_price}\n  Subtotal: ${item.subtotal}\n\n`;
      }
      sb += `--------------------------------\nTOTAL: ${data.total_amount}\n`;
      setSummaryText(sb);
      setTotalAmount(Number(data.total_amount));
      setStatusMsg('Total calculated successfully.');
      return true;
    } catch (err) {
      setError(err.message);
      setStatusMsg('Please review the transaction details.');
      return false;
    }
  };

  const handleConfirmPayment = async () => {
    setError('');
    try {
      const res = await apiRequest('/pos/checkout', {
        method: 'POST',
        body: buildPayload(),
      });
      const d = res.data;
      setReceiptTxNo(d.transaction_no);
      clearFields();
    } catch (err) {
      setError(err.message);
      setStatusMsg('Please review the transaction details.');
    }
  };

  const clearFields = () => {
    const d = defaultSchedule();
    setCottageId('0');
    setCottageStart(d.cottageStart);
    setCottageEnd(d.cottageEnd);
    setCottageGuests('1');
    setRoomId('0');
    setCheckIn(d.checkIn);
    setCheckOut(d.checkOut);
    setRoomGuests('1');
    setPaymentMethod('Cash');
    setAmountReceived('');
    setReferenceNo('');
    setTotalAmount(0);
    setSummaryText('Click CALCULATE TOTAL to view the transaction summary.');
    setStatusMsg('Waiting for calculation');
    setError('');
  };

  const handleReset = () => {
    if (window.confirm('Clear the current transaction form?')) {
      clearFields();
    }
  };

  const receivedNum = Number(amountReceived);
  const hasValidReceived = amountReceived.trim() !== '' && !Number.isNaN(receivedNum) && receivedNum >= 0 && totalAmount > 0;
  const changeAmount = hasValidReceived ? receivedNum - totalAmount : 0;
  const isDigital = paymentMethod !== 'Cash';

  return (
    <div className="page-shell" style={{ background: '#eaf7f7' }}>
      <header className="resort-header" style={{ background: 'linear-gradient(135deg, #009eaa, #006b91)' }}>
        <div>
          <div className="brand">PROMISE RESORT</div>
          <h1>POINT OF SALE</h1>
          <div className="subtitle">
            Process cottage rentals, room bookings, and payments
          </div>
        </div>
        <div className="header-actions">
          <span className="badge-outline">CASHIER POS</span>
          <button className="btn btn-gray" onClick={() => navigate('/')}>
            ← DASHBOARD
          </button>
        </div>
      </header>

      <main className="page-content">
        {error && <div className="alert alert-error">{error}</div>}

        <div className="split-layout" style={{ gridTemplateColumns: '1fr 365px' }}>
          {/* Left Form Sections */}
          <div>
            <div style={{ marginBottom: 14 }}>
              <h2 style={{ fontSize: 21 }}>Create a New Transaction</h2>
              <p style={{ fontSize: 12, color: '#5b7780' }}>
                Select a customer and the cottage or room they want to avail.
              </p>
            </div>

            {/* 01 CUSTOMER */}
            <SectionCard
              no="01"
              title="CUSTOMER INFORMATION"
              subtitle="Choose the customer for this transaction."
            >
              <div style={{ display: 'flex', gap: 8 }}>
                <select
                  className="select"
                  value={customerId}
                  onChange={(e) => setCustomerId(e.target.value)}
                >
                  {customers.map((c) => (
                    <option key={c.customer_id} value={c.customer_id}>
                      {c.full_name} {c.contact_number ? `- ${c.contact_number}` : ''}
                    </option>
                  ))}
                </select>
                <button
                  type="button"
                  className="btn btn-green"
                  onClick={() => navigate('/customers/register')}
                >
                  + NEW
                </button>
              </div>
            </SectionCard>

            {/* 02 COTTAGE RENTAL */}
            <SectionCard
              no="02"
              title="COTTAGE RENTAL"
              subtitle="Optional. Select a cottage and rental period."
            >
              <div className="form-group">
                <label>Cottage</label>
                <select
                  className="select"
                  value={cottageId}
                  onChange={(e) => setCottageId(e.target.value)}
                >
                  <option value="0">-- None --</option>
                  {cottages.map((c) => (
                    <option key={c.cottage_id} value={c.cottage_id}>
                      {c.cottage_name} ({c.cottage_type}, {c.capacity} pax) - ₱{c.rental_price}
                    </option>
                  ))}
                </select>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 110px', gap: 12 }}>
                <div className="form-group">
                  <label>Start Date / Time</label>
                  <input
                    className="input"
                    value={cottageStart}
                    onChange={(e) => setCottageStart(e.target.value)}
                    placeholder="yyyy-MM-dd HH:mm"
                  />
                </div>
                <div className="form-group">
                  <label>End Date / Time</label>
                  <input
                    className="input"
                    value={cottageEnd}
                    onChange={(e) => setCottageEnd(e.target.value)}
                    placeholder="yyyy-MM-dd HH:mm"
                  />
                </div>
                <div className="form-group">
                  <label>Cottage Guests</label>
                  <input
                    className="input"
                    value={cottageGuests}
                    onChange={(e) => setCottageGuests(e.target.value)}
                  />
                </div>
              </div>
            </SectionCard>

            {/* 03 ROOM BOOKING */}
            <SectionCard
              no="03"
              title="ROOM BOOKING"
              subtitle="Optional. Select a room and stay details."
            >
              <div className="form-group">
                <label>Room</label>
                <select
                  className="select"
                  value={roomId}
                  onChange={(e) => setRoomId(e.target.value)}
                >
                  <option value="0">-- None --</option>
                  {rooms.map((r) => (
                    <option key={r.room_id} value={r.room_id}>
                      {r.room_name} ({r.room_type}, {r.max_capacity} pax) - ₱{r.price_per_night}/night
                    </option>
                  ))}
                </select>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 110px', gap: 12 }}>
                <div className="form-group">
                  <label>Check-in</label>
                  <input
                    className="input"
                    value={checkIn}
                    onChange={(e) => setCheckIn(e.target.value)}
                    placeholder="yyyy-MM-dd HH:mm"
                  />
                </div>
                <div className="form-group">
                  <label>Check-out</label>
                  <input
                    className="input"
                    value={checkOut}
                    onChange={(e) => setCheckOut(e.target.value)}
                    placeholder="yyyy-MM-dd HH:mm"
                  />
                </div>
                <div className="form-group">
                  <label>Room Guests</label>
                  <input
                    className="input"
                    value={roomGuests}
                    onChange={(e) => setRoomGuests(e.target.value)}
                  />
                </div>
              </div>
            </SectionCard>

            <div style={{ fontSize: 11, color: '#5b7780', marginTop: 4 }}>
              Date format: yyyy-MM-dd HH:mm | Example: 2026-11-15 09:00
            </div>
          </div>

          {/* Right Order Summary & Payment Card */}
          <div className="card">
            <h3 style={{ fontSize: 18, color: '#006b91' }}>ORDER SUMMARY</h3>
            <p style={{ fontSize: 11, color: '#5b7780', marginBottom: 10 }}>
              Review the charges before accepting payment.
            </p>

            <pre
              style={{
                background: '#eaf7f7',
                border: '1px solid #cde5e5',
                padding: 12,
                fontFamily: 'monospace',
                fontSize: 12,
                minHeight: 165,
                maxHeight: 240,
                overflowY: 'auto',
                whiteSpace: 'pre-wrap',
                marginBottom: 12,
              }}
            >
              {summaryText}
            </pre>

            <div style={{ background: '#eaf7f7', padding: 12, borderRadius: 6, marginBottom: 12 }}>
              <div style={{ fontSize: 11, fontWeight: 700, color: '#5b7780' }}>TOTAL AMOUNT</div>
              <div style={{ fontSize: 28, fontWeight: 700, color: '#006b91', marginTop: 2 }}>
                ₱{totalAmount.toFixed(2)}
              </div>
            </div>

            <div className="form-group">
              <label>PAYMENT METHOD</label>
              <select
                className="select"
                value={paymentMethod}
                onChange={(e) => {
                  setPaymentMethod(e.target.value);
                  if (e.target.value === 'Cash') setReferenceNo('');
                }}
              >
                <option value="Cash">Cash</option>
                <option value="GCash">GCash</option>
                <option value="Maya">Maya</option>
                <option value="MariBank">MariBank</option>
                <option value="BDO">BDO</option>
                <option value="Credit/Debit Card">Credit/Debit Card</option>
              </select>
            </div>

            <div className="form-group">
              <label>AMOUNT RECEIVED</label>
              <input
                className="input"
                value={amountReceived}
                onChange={(e) => setAmountReceived(e.target.value)}
                placeholder="0.00"
              />
            </div>

            {isDigital && (
              <div className="form-group">
                <label>Reference Number</label>
                <input
                  className="input"
                  value={referenceNo}
                  onChange={(e) => setReferenceNo(e.target.value)}
                  placeholder={`Enter ${paymentMethod} reference no.`}
                />
              </div>
            )}

            <div
              style={{
                border: '1px solid #cde5e5',
                padding: '10px 12px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                borderRadius: 6,
                marginBottom: 8,
              }}
            >
              <span style={{ fontSize: 12, fontWeight: 700, color: '#5b7780' }}>CHANGE</span>
              <span
                style={{
                  fontSize: 19,
                  fontWeight: 700,
                  color: changeAmount < 0 ? '#be3741' : '#009eaa',
                }}
              >
                ₱{hasValidReceived ? changeAmount.toFixed(2) : '0.00'}
              </span>
            </div>

            <div style={{ fontSize: 11, color: '#5b7780', marginBottom: 12 }}>{statusMsg}</div>

            <div style={{ display: 'grid', gap: 8 }}>
              <button className="btn btn-blue btn-block" onClick={handleCalculate}>
                CALCULATE TOTAL
              </button>
              <button className="btn btn-teal btn-block" onClick={handleConfirmPayment}>
                CONFIRM PAYMENT
              </button>
              <button className="btn btn-gray btn-block" onClick={handleReset}>
                RESET TRANSACTION
              </button>
            </div>
          </div>
        </div>
      </main>

      <footer className="page-footer">
        <span>PROMISE RESORT | POINT OF SALE SYSTEM</span>
        <strong style={{ color: '#006b91' }}>SECURE CASHIER TRANSACTION</strong>
      </footer>

      <ReceiptModal transactionNo={receiptTxNo} onClose={() => setReceiptTxNo('')} />
    </div>
  );
}

function SectionCard({ no, title, subtitle, children }) {
  return (
    <div className="card" style={{ marginBottom: 12 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
        <div
          style={{
            width: 38,
            height: 34,
            background: '#bdebe6',
            color: '#006b91',
            fontWeight: 700,
            fontSize: 12,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderRadius: 4,
          }}
        >
          {no}
        </div>
        <div>
          <div style={{ fontSize: 14, fontWeight: 700, color: '#006b91' }}>{title}</div>
          <div style={{ fontSize: 11, color: '#5b7780' }}>{subtitle}</div>
        </div>
      </div>
      {children}
    </div>
  );
}
