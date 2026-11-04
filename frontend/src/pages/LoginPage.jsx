import React, { useEffect, useRef, useState } from 'react';
import { apiRequest, setCsrfToken } from '../services/api.js';

/**
 * Pixel-exact port of auth/LoginFrame.java (including OceanPanel Graphics2D canvas)
 */
function OceanCanvas() {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const draw = () => {
      const parent = canvas.parentElement;
      if (!parent) return;
      const dpr = window.devicePixelRatio || 1;
      const w = parent.clientWidth;
      const h = parent.clientHeight;
      if (w === 0 || h === 0) return;

      canvas.width = w * dpr;
      canvas.height = h * dpr;
      canvas.style.width = `${w}px`;
      canvas.style.height = `${h}px`;

      const ctx = canvas.getContext('2d');
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

      const s = Math.min(w / 660.0, h / 540.0);

      // Background white (shows on the top-right and bottom-right outside the ellipse clip)
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, w, h);

      // Shape blob = new Ellipse2D.Double(-w * 0.45, -h * 0.15, w * 1.45, h * 1.3)
      ctx.save();
      const rx = (w * 1.45) / 2;
      const ry = (h * 1.3) / 2;
      const cx = -w * 0.45 + rx;
      const cy = -h * 0.15 + ry;
      ctx.beginPath();
      ctx.ellipse(cx, cy, rx, ry, 0, 0, Math.PI * 2);
      ctx.clip();

      // Sky gradient: (0,0) rgb(255,214,150) -> (0, h*0.45) rgb(150,225,235)
      const skyGrad = ctx.createLinearGradient(0, 0, 0, h * 0.45);
      skyGrad.addColorStop(0, 'rgb(255, 214, 150)');
      skyGrad.addColorStop(1, 'rgb(150, 225, 235)');
      ctx.fillStyle = skyGrad;
      ctx.fillRect(0, 0, w, h);

      // Sun
      const sun = Math.floor(90 * s);
      const sx = Math.floor(w * 0.55);
      const sy = Math.floor(h * 0.20);
      const pad = Math.floor(20 * s);

      ctx.fillStyle = 'rgba(255, 230, 160, 0.353)';
      ctx.beginPath();
      ctx.arc(sx + sun / 2, sy + sun / 2, sun / 2 + pad, 0, Math.PI * 2);
      ctx.fill();

      ctx.fillStyle = 'rgba(255, 244, 200, 0.902)';
      ctx.beginPath();
      ctx.arc(sx + sun / 2, sy + sun / 2, sun / 2, 0, Math.PI * 2);
      ctx.fill();

      // Horizon ocean gradient: OCEAN_LIGHT (120,220,215) -> OCEAN_DARK (0,105,148)
      const horizon = Math.floor(h * 0.48);
      const oceanGrad = ctx.createLinearGradient(0, horizon, 0, h);
      oceanGrad.addColorStop(0, 'rgb(120, 220, 215)');
      oceanGrad.addColorStop(1, 'rgb(0, 105, 148)');
      ctx.fillStyle = oceanGrad;
      ctx.fillRect(0, horizon, w, h - horizon);

      // 4 Sine waves
      const drawWave = (baseY, amp, freq, phase, color) => {
        ctx.beginPath();
        ctx.moveTo(0, h);
        for (let x = 0; x <= w; x += 4) {
          const y = baseY + Math.sin(x * freq + phase) * amp;
          ctx.lineTo(x, y);
        }
        ctx.lineTo(w, h);
        ctx.closePath();
        ctx.fillStyle = color;
        ctx.fill();
      };

      drawWave(horizon + Math.floor(15 * s), Math.floor(10 * s), 0.020 / s, 0.0, 'rgba(255, 255, 255, 0.275)');
      drawWave(horizon + Math.floor(55 * s), Math.floor(14 * s), 0.016 / s, 1.5, 'rgba(0, 170, 190, 0.471)');
      drawWave(horizon + Math.floor(105 * s), Math.floor(18 * s), 0.013 / s, 3.0, 'rgba(0, 130, 170, 0.588)');
      drawWave(horizon + Math.floor(160 * s), Math.floor(22 * s), 0.011 / s, 4.5, 'rgba(0, 95, 140, 0.745)');

      // Sand island: Ellipse2D.Double(w * 0.52, h * 0.80, w * 0.7, h * 0.4)
      ctx.fillStyle = 'rgb(244, 222, 170)';
      ctx.beginPath();
      ctx.ellipse(
        w * 0.52 + (w * 0.7) / 2,
        h * 0.80 + (h * 0.4) / 2,
        (w * 0.7) / 2,
        (h * 0.4) / 2,
        0,
        0,
        Math.PI * 2
      );
      ctx.fill();

      // Palm tree: drawPalm(g2, (int) (w * 0.72), (int) (h * 0.88), s)
      const px = Math.floor(w * 0.72);
      const py = Math.floor(h * 0.88);
      ctx.strokeStyle = 'rgb(120, 80, 50)';
      ctx.lineWidth = 7 * s;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
      ctx.beginPath();
      ctx.moveTo(px, py);
      ctx.quadraticCurveTo(px + 14 * s, py - 55 * s, px + 6 * s, py - 110 * s);
      ctx.stroke();

      // Palm leaves
      const tx = px + 6 * s;
      const ty = py - 110 * s;
      ctx.strokeStyle = 'rgb(30, 130, 80)';
      ctx.lineWidth = 4 * s;
      const ends = [
        [-60, 10],
        [-45, -25],
        [-10, -42],
        [35, -35],
        [62, 5],
        [45, 35],
      ];
      for (const [ex, ey] of ends) {
        ctx.beginPath();
        ctx.moveTo(tx, ty);
        ctx.quadraticCurveTo(tx + (ex * s) / 2.0, ty + (ey - 25) * s, tx + ex * s, ty + ey * s);
        ctx.stroke();
      }

      // Coconuts
      ctx.fillStyle = 'rgb(90, 55, 30)';
      const r = Math.floor(8 * s) / 2;
      ctx.beginPath();
      ctx.arc(tx - 6 * s + r, ty + r, r, 0, Math.PI * 2);
      ctx.fill();
      ctx.beginPath();
      ctx.arc(tx + 2 * s + r, ty + 2 * s + r, r, 0, Math.PI * 2);
      ctx.fill();

      ctx.restore();

      // Header text overlay
      ctx.fillStyle = 'rgba(255, 255, 255, 0.92)';
      ctx.font = `700 ${Math.floor(26 * s)}px "Segoe UI", sans-serif`;
      ctx.fillText('Relax. Reserve. Enjoy.', Math.floor(w * 0.30), Math.floor(h * 0.12));

      ctx.fillStyle = 'rgba(255, 255, 255, 0.82)';
      ctx.font = `400 ${Math.floor(15 * s)}px "Segoe UI", sans-serif`;
      ctx.fillText(
        'Promise Resort Management',
        Math.floor(w * 0.30),
        Math.floor(h * 0.12) + Math.floor(26 * s)
      );
    };

    draw();
    window.addEventListener('resize', draw);
    return () => window.removeEventListener('resize', draw);
  }, []);

  return (
    <div className="login-ocean" style={{ position: 'relative', width: '100%', height: '100%', background: '#ffffff' }}>
      <canvas ref={canvasRef} style={{ display: 'block', width: '100%', height: '100%' }} />
    </div>
  );
}

export default function LoginPage({ onLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!username.trim() || !password) {
      setError('Please enter both username and password.');
      return;
    }

    setLoading(true);
    try {
      const res = await apiRequest('/auth/login', {
        method: 'POST',
        body: { username: username.trim(), password },
      });
      setCsrfToken(res.data.csrf_token);
      onLogin(res.data.user);
    } catch (err) {
      setError(err.message);
      setPassword('');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="login-split"
      style={{
        minHeight: '100vh',
        display: 'grid',
        gridTemplateColumns: '60% 40%',
        background: '#ffffff',
      }}
    >
      <div
        className="login-form-side"
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '30px 40px',
          background: '#ffffff',
        }}
      >
        <form
          onSubmit={handleSubmit}
          style={{
            width: '100%',
            maxWidth: 440,
          }}
        >
          <div
            style={{
              color: 'rgb(0, 150, 170)',
              fontWeight: 700,
              fontSize: 13,
              fontFamily: '"Segoe UI", sans-serif',
              marginBottom: 6,
            }}
          >
            PROMISE BEACH RESORT
          </div>

          <h1
            style={{
              fontSize: 34,
              fontWeight: 700,
              color: 'rgb(20, 70, 90)',
              fontFamily: '"Segoe UI", sans-serif',
              margin: '0 0 4px 0',
              lineHeight: 1.15,
            }}
          >
            Welcome Back
          </h1>

          <p
            style={{
              fontSize: 13,
              color: 'rgb(110, 140, 150)',
              fontFamily: '"Segoe UI", sans-serif',
              margin: '0 0 28px 0',
            }}
          >
            Sign in to the Point-of-Sale system
          </p>

          {error && <div className="alert alert-error">{error}</div>}

          <div style={{ marginBottom: 16 }}>
            <label
              htmlFor="username"
              style={{
                display: 'block',
                fontSize: 13,
                fontWeight: 700,
                color: 'rgb(20, 70, 90)',
                marginBottom: 6,
              }}
            >
              Username
            </label>
            <input
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoFocus
              style={{
                width: '100%',
                height: 44,
                padding: '0 14px',
                fontSize: 15,
                color: 'rgb(20, 70, 90)',
                background: 'rgb(236, 247, 248)',
                border: '1px solid rgb(200, 228, 230)',
                borderRadius: 2,
                outline: 'none',
              }}
            />
          </div>

          <div style={{ marginBottom: 8 }}>
            <label
              htmlFor="password"
              style={{
                display: 'block',
                fontSize: 13,
                fontWeight: 700,
                color: 'rgb(20, 70, 90)',
                marginBottom: 6,
              }}
            >
              Password
            </label>
            <input
              id="password"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              style={{
                width: '100%',
                height: 44,
                padding: '0 14px',
                fontSize: 15,
                color: 'rgb(20, 70, 90)',
                background: 'rgb(236, 247, 248)',
                border: '1px solid rgb(200, 228, 230)',
                borderRadius: 2,
                outline: 'none',
              }}
            />
          </div>

          <label
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: 6,
              fontSize: 12,
              color: 'rgb(110, 140, 150)',
              cursor: 'pointer',
              margin: '4px 0 24px 0',
              userSelect: 'none',
            }}
          >
            <input
              type="checkbox"
              checked={showPassword}
              onChange={(e) => setShowPassword(e.target.checked)}
              style={{ accentColor: 'rgb(0, 150, 170)' }}
            />
            Show password
          </label>

          <button
            type="submit"
            disabled={loading}
            style={{
              width: '100%',
              height: 46,
              borderRadius: 46,
              border: 'none',
              cursor: loading ? 'not-allowed' : 'pointer',
              fontSize: 15,
              fontWeight: 700,
              color: '#ffffff',
              background: 'linear-gradient(90deg, rgb(0, 150, 170), rgb(0, 105, 148))',
              letterSpacing: 0.3,
              transition: 'filter 0.15s ease',
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.background =
                'linear-gradient(90deg, rgb(0, 150, 170), rgb(120, 220, 215))';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.background =
                'linear-gradient(90deg, rgb(0, 150, 170), rgb(0, 105, 148))';
            }}
          >
            {loading ? 'SIGNING IN...' : 'LOGIN'}
          </button>
        </form>
      </div>

      <OceanCanvas />
    </div>
  );
}
