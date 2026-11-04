import React, { useEffect, useState } from 'react';
import { apiRequest } from '../services/api.js';

function centerText(text, width = 40) {
  const pad = Math.max(0, Math.floor((width - text.length) / 2));
  return ' '.repeat(pad) + text;
}

function moneyLine(label, val) {
  const num = Number(val || 0).toFixed(2);
  return label.padEnd(20, ' ') + num.padStart(20, ' ') + '\n';
}

export function buildReceiptText(tx) {
  const line = '----------------------------------------\n';
  let out = '';
  out += centerText('PROMISE RESORT') + '\n';
  out += centerText('OFFICIAL RECEIPT') + '\n';
  out += line;
  out += `Transaction No: ${tx.transaction_no}\n`;
  out += `Date: ${tx.formatted_date || tx.transaction_date}\n`;
  out += `Customer: ${tx.customer_name}\n`;
  out += `Cashier: ${tx.cashier_name}\n`;
  out += `Payment: ${tx.payment_method}${tx.reference_no ? ` (Ref: ${tx.reference_no})` : ''}\n`;
  out += line;

  for (const it of tx.items || []) {
    out += `${it.description}\n`;
    const left = `  ${it.quantity} x ${Number(it.unit_price).toFixed(2)}`;
    const right = Number(it.subtotal).toFixed(2);
    const spaces = Math.max(1, 40 - left.length - right.length);
    out += left + ' '.repeat(spaces) + right + '\n';
  }

  out += line;
  out += moneyLine('Subtotal:', tx.subtotal);
  out += moneyLine('Discount:', tx.discount);
  out += moneyLine('TOTAL:', tx.total_amount);
  out += moneyLine('Cash Received:', tx.amount_received);
  out += moneyLine('Change:', tx.change_amount);
  out += line;
  out += centerText('Thank you! Please come back again!') + '\n';
  return out;
}

export default function ReceiptModal({ transactionNo, onClose }) {
  const [tx, setTx] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!transactionNo) return;
    setError('');
    setTx(null);
    apiRequest(`/transactions/${encodeURIComponent(transactionNo)}`)
      .then((res) => setTx(res.data))
      .catch((err) => setError(err.message));
  }, [transactionNo]);

  if (!transactionNo) return null;

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="receipt-modal printable-area" onClick={(e) => e.stopPropagation()}>
        <div
          className="no-print"
          style={{
            background: '#006b91',
            color: '#fff',
            padding: '12px 16px',
            fontWeight: 700,
            display: 'flex',
            justifyContent: 'space-between',
          }}
        >
          <span>Receipt - {transactionNo}</span>
          <button
            onClick={onClose}
            style={{ background: 'transparent', border: 'none', color: '#fff', cursor: 'pointer', fontSize: 16 }}
          >
            ✕
          </button>
        </div>

        <pre className="receipt-pre">
          {error ? `Error: ${error}` : !tx ? 'Loading receipt...' : buildReceiptText(tx)}
        </pre>

        <div className="no-print" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, padding: 10 }}>
          <button className="btn btn-blue" onClick={() => window.print()} disabled={!tx}>
            PRINT
          </button>
          <button className="btn btn-gray" onClick={onClose}>
            CLOSE
          </button>
        </div>
      </div>
    </div>
  );
}
