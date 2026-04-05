document.addEventListener('DOMContentLoaded', () => {
    // ---- Page Elements ----
    const loginForm = document.getElementById('loginForm');
    const signupForm = document.getElementById('signupForm');
    const verifyInputs = document.querySelectorAll('.otp-input');
    const verifyBtn = document.querySelector('.auth-card h2')?.innerText === 'Verify Identity' ? document.querySelector('.btn-primary') : null;
    const resendLink = document.querySelector('.link-action');

    // ---- Utility Functions ----
    const getQueryParam = (param) => new URLSearchParams(window.location.search).get(param);

    const showAlert = (message, type = 'error') => {
        // Modern alert implementation
        const alertBox = document.createElement('div');
        alertBox.className = `alert-popup ${type}`;
        alertBox.innerText = message;
        document.body.appendChild(alertBox);
        
        setTimeout(() => {
            alertBox.classList.add('show');
        }, 10);

        setTimeout(() => {
            alertBox.classList.remove('show');
            setTimeout(() => alertBox.remove(), 300);
        }, 3000);
    };

    // ---- OTP UI Logic ----
    if (verifyInputs.length > 0) {
        verifyInputs.forEach((input, index) => {
            input.addEventListener('input', (e) => {
                if (e.target.value.length === 1 && index < verifyInputs.length - 1) {
                    verifyInputs[index + 1].focus();
                }
            });

            input.addEventListener('keydown', (e) => {
                if (e.key === 'Backspace' && !e.target.value && index > 0) {
                    verifyInputs[index - 1].focus();
                }
            });

            input.addEventListener('keypress', (e) => {
                if (!/[0-9]/.test(e.key)) {
                    e.preventDefault();
                }
            });
        });
    }

    // ---- Auth Logic ----

    // LOGIN
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;

            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });

                if (response.ok) {
                    const data = await response.json();
                    
                    // CHECK FOR DEV-MODE OTP BYPASS
                    if (data.token) {
                        localStorage.setItem('token', data.token);
                        localStorage.setItem('user', JSON.stringify(data.user));
                        showAlert('Verification Bypassed: Access Granted.', 'success');
setTimeout(() => {
window.location.href = '/chat.html';
                        }, 800);
                        return;
                    }

                    showAlert('OTP Sent! Transmission secure.', 'success');
                    setTimeout(() => {
                        window.location.href = `verify.html?email=${encodeURIComponent(email)}`;
                    }, 800);
                } else {
                    let errorMessage = 'Access Denied';
                    try {
                        const data = await response.json();
                        errorMessage = data.message || data.error || errorMessage;
                    } catch(e) {
                        errorMessage = await response.text() || errorMessage;
                    }
                    showAlert(errorMessage);
                }
            } catch (err) {
                showAlert('Connection error. Is the server running?');
            }
        });
    }

    // SIGNUP
    if (signupForm) {
        signupForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const fullName = document.getElementById('fullName').value;
            const email = document.getElementById('email').value;
            const phone = document.getElementById('phone').value;
            const password = document.getElementById('password').value;

            const username = email.split('@')[0]; 

            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ name: fullName, email, phone, password, username, role: 'USER' })
                });

                if (response.ok) {
                    showAlert('Account Created! Welcome to the Grid.', 'success');
                    // Automatically log in after registration
                    const loginRes = await fetch('/api/auth/login', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ email, password })
                    });
                    if(loginRes.ok) {
                        const data = await loginRes.json();
                        if (data.token) {
                            localStorage.setItem('token', data.token);
                            localStorage.setItem('user', JSON.stringify(data.user));
                            setTimeout(() => window.location.href = '/chat.html', 800);
                        } else {
                            showAlert('OTP Sent! Transmission secure.', 'success');
                            setTimeout(() => {
                                window.location.href = `verify.html?email=${encodeURIComponent(email)}`;
                            }, 800);
                        }
                    } else {
                        setTimeout(() => {
                            window.location.href = `verify.html?email=${encodeURIComponent(email)}`;
                        }, 1500);
                    }
                } else {
                    let errorMessage = 'Registration Failed';
                    try {
                        const data = await response.json();
                        errorMessage = data.message || data.error || errorMessage;
                    } catch(e) {
                        errorMessage = await response.text() || errorMessage;
                    }
                    showAlert(errorMessage);
                }
            } catch (err) {
                showAlert('Connection error.');
            }
        });
    }

    // VERIFY OTP
    if (verifyBtn) {
        verifyBtn.addEventListener('click', async () => {
            const email = getQueryParam('email');
            if (!email) {
                showAlert('Email is missing in URL');
                return;
            }

            const otp = Array.from(verifyInputs).map(i => i.value).join('');
            if (otp.length !== 6) {
                showAlert('Please enter the 6-digit code');
                return;
            }

            try {
                const res = await fetch('/api/auth/verify-otp', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, otp })
                });

                if (res.ok) {
                    const data = await res.json();
                    localStorage.setItem('token', data.token);
                    localStorage.setItem('user', JSON.stringify(data.user));
                    showAlert('Identity verified!', 'success');
                    setTimeout(() => {
window.location.href = '/chat.html';
                    }, 1000);
                } else {
                    let errorMessage = 'Verification Failed';
                    try {
                        const data = await res.json();
                        errorMessage = data.message || data.error || errorMessage;
                    } catch(e) {
                        errorMessage = await res.text() || errorMessage;
                    }
                    showAlert(errorMessage);
                }
            } catch (err) {
                showAlert('Verification failed.');
            }
        });

        if (resendLink) {
            resendLink.addEventListener('click', async (e) => {
                e.preventDefault();
                const email = getQueryParam('email');
                if (!email) return;

                try {
                    const response = await fetch('/api/auth/send-otp', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ email })
                    });
                    if (response.ok) {
                        showAlert('Code resent!', 'success');
                    } else {
                        showAlert('Wait before requesting again');
                    }
                } catch (err) {
                    showAlert('Error resending code');
                }
            });
        }
    }

    // FORGOT PASSWORD
    const forgotForm = document.getElementById('forgotForm');
    if (forgotForm) {
        forgotForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('email').value;
            try {
                const response = await fetch('/api/auth/send-otp', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email })
                });
                if (response.ok) {
                    showAlert('Recovery code transmitted!', 'success');
                    setTimeout(() => window.location.href = `reset.html?email=${encodeURIComponent(email)}`, 1000);
                } else {
                    const data = await response.json();
                    showAlert(data.message || 'Transmission failed.');
                }
            } catch (err) {
                showAlert('Grid offline. Connection failed.');
            }
        });
    }

    // RESET PASSWORD
    const resetForm = document.getElementById('resetForm');
    if (resetForm) {
        resetForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = getQueryParam('email');
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmNewPassword').value;
            const otp = Array.from(verifyInputs).map(i => i.value).join('');

            if (otp.length !== 6) {
                showAlert('Verification code incomplete.');
                return;
            }
            if (newPassword !== confirmPassword) {
                showAlert('Credential mismatch. Passwords do not match.');
                return;
            }

            try {
                const response = await fetch('/api/auth/reset-password', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, otp, newPassword })
                });
                if (response.ok) {
                    showAlert('Credential reset success!', 'success');
                    setTimeout(() => window.location.href = 'login.html', 1500);
                } else {
                    const data = await response.json();
                    showAlert(data.message || 'Identity verification failed.');
                }
            } catch (err) {
                showAlert('Sync failure. Connection lost.');
            }
        });
    }

    // ---- Visual Effects ----
    const primaryBtns = document.querySelectorAll('.btn-primary');
    primaryBtns.forEach(btn => {
        btn.addEventListener('mousedown', () => btn.style.transform = 'translateY(1px) scale(0.98)');
        btn.addEventListener('mouseup', () => btn.style.transform = 'translateY(-2px) scale(1)');
    });

    // ---- Theme Toggle Logic ----
    const themeToggle = document.getElementById('themeToggle');
    const root = document.documentElement;
    const currentTheme = localStorage.getItem('theme') || 'dark';

    root.setAttribute('data-theme', currentTheme);

    if (themeToggle) {
        updateThemeIcon(currentTheme);
        themeToggle.addEventListener('click', () => {
            const theme = root.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
            root.setAttribute('data-theme', theme);
            localStorage.setItem('theme', theme);
            updateThemeIcon(theme);
        });
    }

    function updateThemeIcon(theme) {
        if (!themeToggle) return;
        const icon = themeToggle.querySelector('svg');
        if (theme === 'light') {
            // Sun icon
            icon.innerHTML = '<path d="M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10zM2 13h2a1 1 0 1 0 0-2H2a1 1 0 1 0 0 2zm18 0h2a1 1 0 1 0 0-2h-2a1 1 0 1 0 0 2zM11 2v2a1 1 0 1 0 2 0V2a1 1 0 1 0-2 0zm0 18v2a1 1 0 1 0 2 0v-2a1 1 0 1 0-2 0zM5.99 4.58a1 1 0 1 0-1.41 1.41l1.06 1.06a1 1 0 1 0 1.41-1.41L5.99 4.58zm12.37 12.37a1 1 0 1 0-1.41 1.41l1.06 1.06a1 1 0 1 0 1.41-1.41l-1.06-1.06zm1.06-12.37a1 1 0 1 0-1.41-1.41l-1.06 1.06a1 1 0 1 0 1.41 1.41l1.06-1.06zM5.99 18.01a1 1 0 1 0-1.41-1.41l-1.06 1.06a1 1 0 1 0 1.41 1.41l1.06-1.06z"/>';
        } else {
            // Moon icon
            icon.innerHTML = '<path d="M12 3c.132 0 .263 0 .393.007a9 9 0 0 0 9.6 9.6A9 9 0 1 1 12 3z"/>';
        }
    }
});
