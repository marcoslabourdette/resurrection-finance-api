let stompClient = null;

window.onload = function () {
    const token = localStorage.getItem('jwt_token');
    const userEmail = localStorage.getItem('user_email');
    const userName = localStorage.getItem('user_name');
    const navActions = document.getElementById('nav-actions-wrapper');
    const lang = localStorage.getItem('user_lang') || 'es';
    if (token) {
        document.getElementById('login-section').style.display = 'none';
        document.getElementById('dashboard-section').style.display = 'block';
        connectWebSocket(userEmail);
        updateBalanceDisplay();
        updateRecentMovements();
        if (navActions) navActions.style.display = 'flex';
        if (userName) {
            const welcomeText = translations[lang].welcome;
            document.getElementById('pilot-name').innerText = `${welcomeText}, ${userName.toUpperCase()}`;
        }
        else if (userEmail) {
            const shortName = userEmail.split('@')[0];
            document.getElementById('pilot-name').innerText = `BIENVENIDO, ${shortName.toUpperCase()}`;
        }
    } else {
        document.getElementById('login-section').style.display = 'block';
        document.getElementById('dashboard-section').style.display = 'none';
        if (navActions) navActions.style.display = 'none';
    }
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('logout') === 'true') {
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('user_email');
        window.history.replaceState({}, document.title, "/index.html");
        location.reload();
    }
    window.onhashchange = function () {
        const hash = window.location.hash;
        document.documentElement.classList.remove('show-register');

        if (hash === '#register') {
            toggleAuthMode('register');
        } else {
            toggleAuthMode('login');
        }
    };
};

function connectWebSocket(userEmail) {
    const socket = new SockJS('http://localhost:8081/ws-messenger');
    stompClient = Stomp.over(socket);
    const token = localStorage.getItem('jwt_token');
    stompClient.connect({ 'Authorization': 'Bearer ' + token }, function (frame) {
        console.log("[GATEWAY]: WebSocket connection established. Protocol handshake synchronized.", frame);
        stompClient.subscribe('/user/queue/notifications', function (payload) {
            console.log("[COMMUNICATION]: Inbound encrypted private message received.");
            updateBalanceDisplay();
            updateRecentMovements();
        });
    }, function (error) {
        console.error("[SECURITY]: Request intercepted and rejected by the authentication middleware:", error);
    });
}

async function handleLogin() {
    const email = document.getElementById('email').value;
    const pass = document.getElementById('password').value;
    await login(email, pass);
}

async function login(email, password) {
    const lang = localStorage.getItem('user_lang') || 'es';
    const t = translations[lang];
    if (!email || !password || email.length > 50 || password.length > 100) {
        const validationMsg = lang === 'es'
            ? "🚨 ERROR: Formato de credenciales inválido para el búnker."
            : "🚨 ERROR: Invalid credential format for the platform.";
        showBunkerMessage(validationMsg, true);
        return;
    }
    const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });
    if (response.ok) {
        const data = await response.json();
        if (data.token) {
            localStorage.setItem('user_name', data.name)
            localStorage.setItem('jwt_token', data.token);
            localStorage.setItem('user_email', email);
            if (data.role) { localStorage.setItem('user_role', data.role); }
            if (data.externalId) {
                localStorage.setItem('user_external_id', data.externalId);
            }
            connectWebSocket(email);
            updateBalanceDisplay();
            updateRecentMovements();
            document.getElementById('login-section').style.display = 'none';
            document.getElementById('dashboard-section').style.display = 'block';
            if (data.role === 'ADMIN' || data.role === 'ROLE_ADMIN') {
                document.documentElement.classList.add('is-admin');
                console.log("🕵️‍♂️ [SECURITY]: " + (lang === 'es' ? "NIVEL ADMIN DETECTADO" : "ADMIN ACCESS DETECTED"));
            }
            const navActions = document.getElementById('nav-actions-wrapper');
            if (navActions) navActions.style.display = 'flex';
            const shortName = email.split('@')[0];
            document.getElementById('pilot-name').innerText = `BIENVENIDO, ${shortName.toUpperCase()}`;
            console.log("✅ SUCCESSFUL LOGIN: Identity synchronized in the local bunker.");
            location.reload();
        }
    } else {
        const errorMsg = lang === 'es'
            ? "🚨 ACCESO DENEGADO: Credenciales de acceso inválidas."
            : "🚨 ACCESS DENIED: Invalid login credentials.";

        showBunkerMessage(errorMsg, true);
    }
}

function goToMessenger() {
    const token = localStorage.getItem('jwt_token');
    if (!token) {
        showBunkerMessage("🚨 ERROR: Token no detectado", true);
        return;
    }
    const email = localStorage.getItem('user_email');
    const externalId = localStorage.getItem('user_external_id');
    const currentLang = localStorage.getItem('user_lang') || 'es';
    window.location.href = `http://localhost:8081/index.html?token=${token}&email=${email}&external_id=${externalId}&lang=${currentLang}`;
}
function backToBank() {
    window.location.href = "http://localhost:8080/index.html";
}

let currentTransferMode = 'email';

function setTransferMode(mode) {
    currentTransferMode = mode;
    updateLanguageUI();
    const lang = localStorage.getItem('user_lang') || 'es';
    const input = document.getElementById('receiver');
    const btnEmail = document.getElementById('btn-email');
    const btnCvu = document.getElementById('btn-cvu');
    input.value = "";
    const preview = document.getElementById('receiver-preview');
    if (preview) preview.innerText = "";
    if (mode === 'email') {
        input.placeholder = translations[lang].receiver_placeholder || "Email";
        input.removeAttribute('maxlength');
        btnEmail.style.background = "var(--primary-blue)"; btnEmail.style.color = "white";
        btnCvu.style.background = "#eee"; btnCvu.style.color = "#666";
    } else {
        input.placeholder = translations[lang].cvu_placeholder || "CVU (22 digits)";
        input.setAttribute('maxlength', '22');
        btnEmail.style.background = "#eee"; btnEmail.style.color = "#666";
        btnCvu.style.background = "var(--primary-blue)"; btnCvu.style.color = "white";
    }
}

async function handleTransfer() {
    const lang = localStorage.getItem('user_lang') || 'es';
    const t = translations[lang];
    const receiver = document.getElementById('receiver').value;
    const amount = document.getElementById('amount').value;
    const description = document.getElementById('description').value;

    if (description.length > 30) {
        const descError = lang === 'es'
            ? "🚨 La descripción no puede superar los 30 caracteres."
            : "🚨 Description cannot exceed 30 characters.";
        showBunkerMessage(descError, true);
        return;
    }

    const token = localStorage.getItem('jwt_token');
    const endpoint = (currentTransferMode === 'email')
        ? '/api/transactions/send-by-email'
        : `/api/transactions/${localStorage.getItem('user_external_id')}`;

    const parsedAmount = parseFloat(amount);

    if (isNaN(parsedAmount) || parsedAmount <= 0) {
        const amountError = lang === 'es'
            ? "🚨 El monto debe ser superior a cero."
            : "🚨 Amount must be greater than zero.";
        showBunkerMessage(amountError, true);
        return;
    }

    if (currentTransferMode === 'email' && receiver.length > 50) {
        const emailError = lang === 'es'
            ? "🚨 El email de destino es demasiado largo para el búnker."
            : "🚨 Recipient email is too long for the platform.";
        showBunkerMessage(emailError, true);
        return;
    }

    const bodyData = (currentTransferMode === 'email')
        ? { senderEmail: localStorage.getItem('user_email'), receiverEmail: receiver, amount: parsedAmount, description }
        : { destinationCvu: receiver, amount: parsedAmount, description };

    const response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' },
        body: JSON.stringify(bodyData)
    });

    if (response.ok) {
        const successMsg = lang === 'es'
            ? "🚀 Operación finalizada: Su transferencia ha sido procesada con éxito."
            : "🚀 Transaction complete: Your transfer has been processed successfully.";
        showBunkerMessage(successMsg);
        document.getElementById('receiver').value = '';
        document.getElementById('amount').value = '';
        document.getElementById('description').value = '';
        const preview = document.getElementById('receiver-preview');
        if (preview) preview.innerText = '';
        updateBalanceDisplay();
        updateRecentMovements();
    }
    else {
        const errorData = await response.json();
        const rawMsg = (errorData.error || errorData.message || "").toLowerCase();

        const errorTranslations = {
            es: {
                self_transfer: "No puede realizar transferencias a su propia cuenta.",
                insufficient: "Fondos insuficientes para completar la operación.",
                not_found: "🚨 ERROR: El destinatario no existe en el búnker."
            },
            en: {
                self_transfer: "You cannot transfer funds to your own account.",
                insufficient: "Insufficient funds to complete this transaction.",
                not_found: "🚨 ERROR: Recipient not found in the platform."
            }
        };

        let finalMsg = errorData.error || errorData.message || "ERROR";
        if (rawMsg.includes("no encontrado") || rawMsg.includes("not found") || rawMsg.includes("inexistente")) {
            finalMsg = errorTranslations[lang].not_found;
        }
        else if (rawMsg.includes("mismo") || rawMsg.includes("destinatario") || rawMsg.includes("own account")) {
            finalMsg = errorTranslations[lang].self_transfer;
        }
        else if (rawMsg.includes("insuficiente") || rawMsg.includes("funds")) {
            finalMsg = errorTranslations[lang].insufficient;
        }
        showBunkerMessage(finalMsg, true);
    }

}

async function handleRegister() {
    const lang = localStorage.getItem('user_lang') || 'es';
    const t = translations[lang];
    const name = document.getElementById('reg-name').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const phone = document.getElementById('reg-phone').value;
    const address = document.getElementById('reg-address').value;
    const income = document.getElementById('reg-income').value;
    const parsedIncome = parseFloat(income);

    if (name.length < 2 || name.length > 20) {
        const nameError = lang === 'es'
            ? "🚨 El nombre debe tener entre 2 y 20 caracteres."
            : "🚨 Name must be between 2 and 20 characters long.";
        showBunkerMessage(nameError, true);
        return;
    }
    if (email.length > 50) {
        const emailError = lang === 'es'
            ? "🚨 El correo electrónico es demasiado largo para el búnker."
            : "🚨 Email address is too long for the platform.";
        showBunkerMessage(emailError, true);
        return;
    }
    if (password.length < 6 || password.length > 100) {
        const passError = lang === 'es'
            ? "🚨 La contraseña debe tener entre 6 y 100 caracteres."
            : "🚨 Password must be between 6 and 100 characters long.";
        showBunkerMessage(passError, true);
        return;
    }
    if (isNaN(parsedIncome) || parsedIncome <= 0 || parsedIncome > 99999999.99) {
        const incomeError = lang === 'es'
            ? "🚨 El ingreso mensual debe ser un número válido entre 1 y 99.999.999."
            : "🚨 Monthly income must be a valid number between 1 and 99,999,999.";
        showBunkerMessage(incomeError, true);
        return;
    }
    if (phone.length > 20) {
        const phoneError = lang === 'es'
            ? "🚨 El teléfono no puede superar los 20 caracteres."
            : "🚨 Phone number cannot exceed 20 characters.";
        showBunkerMessage(phoneError, true);
        return;
    }

    if (address.length > 100) {
        const addrError = lang === 'es'
            ? "🚨 La dirección es demasiado larga (máx. 100)."
            : "🚨 Address is too long (max. 100).";
        showBunkerMessage(addrError, true);
        return;
    }

    const response = await fetch('/api/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            name: name,
            email: email,
            password: password,
            phone: phone,
            address: address,
            monthlyIncome: parseFloat(income)
        })
    });

    if (response.ok) {
        const successMsg = lang === 'es'
            ? "✨ [SISTEMA]: ¡ALTA EXITOSA! Te regalamos $100.00. Ingresá con tus credenciales."
            : "✨ [SYSTEM]: REGISTRATION SUCCESSFUL! $100.00 bonus granted. Please sign in.";

        showBunkerMessage(successMsg);
        toggleAuthMode('login');
    }
    else if (response.status === 409) {
        const conflictMsg = lang === 'es'
            ? "🚨 ERROR: Esa matrícula ya está registrada en el búnker."
            : "🚨 ERROR: This email is already registered in the platform.";
        showBunkerMessage(conflictMsg, true);
    }
    else {
        const errorData = await response.json();
        let finalMsg = lang === 'es' ? "DATOS INVÁLIDOS" : "INVALID DATA";
        if (errorData) {
            const rawError = Object.values(errorData).join(" | ").toLowerCase();
            if (rawError.includes("contraseña") || rawError.includes("password")) {
                finalMsg = lang === 'es'
                    ? "La contraseña debe tener al menos 6 caracteres."
                    : "Password must be at least 6 characters long.";
            }
            else if (rawError.includes("email") || rawError.includes("correo")) {
                finalMsg = lang === 'es'
                    ? "Formato de correo electrónico inválido."
                    : "Invalid email address format.";
            }
            else {
                finalMsg = Object.values(errorData).join(" | ") || finalMsg;
            }
        }
        showBunkerMessage(`🚨 ${finalMsg}`, true);
    }
}

function toggleAuthMode(mode) {
    const html = document.documentElement;
    if (mode === 'register') {
        html.classList.remove('show-login');
        html.classList.add('show-register');
        window.location.hash = 'register';
    } else {
        html.classList.remove('show-register');
        html.classList.add('show-login');
        window.location.hash = 'login';
    }
}

async function updateBalanceDisplay() {
    const token = localStorage.getItem('jwt_token');
    if (!token) return;

    try {
        const response = await fetch('/api/accounts/my-balance', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (!response.ok) {
            const errorText = await response.text();
            console.error(`🚨 [BAD REQUEST]: ${response.status} - ${errorText}`);
            return;
        }

        const data = await response.json();
        console.log("📊 [DATA RECEIVED]:", data);
        const balanceElement = document.getElementById('balance-amount');
        if (balanceElement) {
            const finalBalance = (typeof data === 'number') ? data : (data.balance || 0);
            balanceElement.innerText = `$${finalBalance.toFixed(2)}`;
        }

        if (data.cvu) localStorage.setItem('user_cvu', data.cvu);

    } catch (error) {
        console.error("❌ Telemetry connection failure:", error);
    }
}
async function updateRecentMovements() {
    const token = localStorage.getItem('jwt_token');
    if (!token) return;

    try {
        const response = await fetch('/api/transactions/recent', {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            }
        });
        if (response.ok) {
            const movements = await response.json();
            const container = document.getElementById('recent-movements-list');
            if (!container) return;
            if (movements.length === 0) {
                const lang = localStorage.getItem('user_lang') || 'es';
                const t = translations[lang];
                container.innerHTML = `
                 <div style="text-align: center; padding: 40px 10px; margin-top: 20px;">
                     <div style="font-size: 3em; margin-bottom: 10px;">🏎️💨</div>
                     <p style="color: #888; font-weight: 800; font-size: 0.9em; margin: 0;">
                         ${t.empty_welcome || '¡BIENVENIDO A LA PISTA, SOCIO!'}
                     </p>
                     <p style="color: #bbb; font-size: 0.85em; margin-top: 8px; line-height: 1.4;">
                         ${t.empty_desc || 'Aún no registramos movimientos en tu búnker.'}<br>
                         ${t.empty_call || '¡Realizá tu primer giro seguro ahora!'}
                     </p>
                 </div>`;
                return;
            }
            container.innerHTML = '';
            movements.forEach(m => {
                const lang = localStorage.getItem('user_lang') || 'es';
                const t = translations[lang];
                const userEmail = localStorage.getItem('user_email');
                const isSent = m.senderEmail === userEmail;
                const icon = isSent ? '↘️' : '↖️';
                const color = isSent ? '#ff4444' : '#2DCC71';
                const sign = isSent ? '-' : '+';
                const prefix = isSent ? (t.sent_to || 'SENT TO:') : (t.received_from || 'FROM:');
                const counterPartyEmail = isSent ? m.receiverEmail : m.senderEmail;
                container.innerHTML += `
                <div style="border-bottom: 1px solid #eee; padding: 20px 0; display: flex; justify-content: space-between; align-items: center; width: 100%;">
                    <div style="display: flex; flex-direction: column; gap: 4px;">
                         <span style="font-weight: 800; color: #004481; font-size: 0.95em; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block; max-width: 250px;">
                             ${icon} ${prefix} ${counterPartyEmail}
                         </span>
                         <span style="color: #888; font-size: 0.9em; font-weight: 600;">
                             🕒 ${new Date(m.timestamp).toLocaleDateString()}
                         </span>
                    </div>
                    <strong style="color: ${color}; font-size: 1.35em; font-weight: 900; letter-spacing: -0.5px;">
                        ${sign}$${m.amount.toFixed(2)}
                    </strong>
                </div>`;
            });
        }
    } catch (error) {
        console.error("❌ Error synchronizing movements:", error);
    }
}
async function loadAdminDashboard() {
    const token = localStorage.getItem('jwt_token');
    const lang = localStorage.getItem('user_lang') || 'es';
    const t = translations[lang];

    const response = await fetch('/api/users/admin/all', {
        headers: { 'Authorization': 'Bearer ' + token }
    });

    if (response.ok) {
        const users = await response.json();
        const list = document.getElementById('admin-user-list');
        list.innerHTML = '';
        users.forEach(user => {
            const statusLabel = user.active ? t.status_active : t.status_inactive;
            const actionBtn = !user.active
                ? `<button onclick="reactivateUser('${user.externalId}')" class="btn-resurrect" data-i18n="btn_activate">${t.btn_activate}</button>`
                : '---';
            list.innerHTML += `
                <tr>
                    <td style="padding: 12px; border-bottom: 1px solid #eee;">${user.name}</td>
                    <td style="padding: 12px; border-bottom: 1px solid #eee;">${user.email}</td>
                    <td style="padding: 12px; border-bottom: 1px solid #eee;">${statusLabel}</td>
                    <td style="padding: 12px; border-bottom: 1px solid #eee;">${actionBtn}</td>
                </tr>
            `;
        });
        const count = document.getElementById('admin-user-count');
        if (count) count.innerText = users.length;
    }
}

function filterAdminUsers() {
    const query = document.getElementById('admin-search-input').value.toLowerCase();
    const rows = document.querySelectorAll('#admin-user-list tr');
    let visibleCount = 0;
    rows.forEach(row => {
        const name = row.cells[0].textContent.toLowerCase();
        const email = row.cells[1].textContent.toLowerCase();
        if (name.includes(query) || email.includes(query)) {
            row.style.display = "";
            visibleCount++;
        } else {
            row.style.display = "none";
        }
    });
    document.getElementById('admin-user-count').innerText = visibleCount;
}

function logoutBanco() {
    const currentLang = localStorage.getItem('user_lang') || 'es';
    console.log("[SECURITY]: Clearing session data and local storage cache.");
    localStorage.clear();
    console.log("[NAVIGATION]: Session terminated. Redirecting to authentication gateway.");
    localStorage.setItem('user_lang', currentLang);
    location.reload();
}
function showBunkerMessage(text, isError = false) {
    const toast = document.getElementById('bunker-toast');
    if (!toast) return;
    toast.style.background = "#ffffff";
    toast.style.color = "#333";
    toast.style.padding = "20px 25px";
    toast.style.borderRadius = "8px";
    toast.style.boxShadow = "0 10px 30px rgba(0,0,0,0.15)";
    toast.style.fontWeight = "600";
    toast.style.fontSize = "0.95em";
    toast.style.minWidth = "300px";

    const statusColor = isError ? "#e74c3c" : "#27ae60";
    toast.style.borderLeft = `8px solid ${statusColor}`;

    const icon = isError ? "⚠️ " : "✅ ";
    toast.innerText = icon + text;

    toast.style.display = 'block';
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(20px)';

    setTimeout(() => {
        toast.style.transition = "all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)";
        toast.style.opacity = '1';
        toast.style.transform = 'translateY(0)';
    }, 10);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-20px)';
        setTimeout(() => { toast.style.display = 'none'; }, 500);
    }, 4000);
}
async function copyCVU() {
    const lang = localStorage.getItem('user_lang') || 'es';
    const t = translations[lang];
    const cvu = localStorage.getItem('user_cvu');
    if (!cvu) {
        const noCvuMsg = lang === 'es'
            ? "🚨 ERROR: CVU no sintonizado todavía"
            : "🚨 ERROR: CVU not synchronized yet";
        showBunkerMessage(noCvuMsg, true);
        return;
    }
    try {
        await navigator.clipboard.writeText(cvu);
        const copySuccess = lang === 'es'
            ? "📋 CVU COPIADO AL PORTAPAPELES"
            : "📋 CVU COPIED TO CLIPBOARD";
        showBunkerMessage(copySuccess);

    } catch (err) {
        console.error("[INTERFACE]: Clipboard copy operation failed:", err);
        const copyError = lang === 'es'
            ? "🚨 ERROR AL COPIAR EL CVU"
            : "🚨 ERROR COPYING CVU";
        showBunkerMessage(copyError, true);
    }
}

async function checkReceiver() {
    const input = document.getElementById('receiver').value;
    const preview = document.getElementById('receiver-preview');
    const submitBtn = document.getElementById('transfer-btn');
    if (input.length === 0) {
        preview.innerText = "";
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.style.opacity = "1";
            submitBtn.style.cursor = "pointer";
        }
        return;
    }
    const token = localStorage.getItem('jwt_token');
    const myCvu = localStorage.getItem('user_cvu');
    const lang = localStorage.getItem('user_lang') || 'es';
    const t = translations[lang];
    const disableBunkerButton = (opacity = "0.5") => {
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.style.opacity = opacity;
            submitBtn.style.cursor = "not-allowed";
        }
    };
    const enableBunkerButton = () => {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.style.opacity = "1";
            submitBtn.style.cursor = "pointer";
        }
    };
    if (currentTransferMode === 'cvu') {
        if (input === myCvu) {
            preview.style.color = "#e67e22";
            preview.innerText = lang === 'es'
                ? "⚠️ NO PUEDES TRANSFERIRTE A TU PROPIA CUENTA"
                : "⚠️ YOU CANNOT TRANSFER TO YOUR OWN ACCOUNT";
            disableBunkerButton();
            return;
        }
        if (input.length === 22) {
            preview.style.color = "#27ae60";
            preview.innerText = lang === 'es' ? "🔍 ESCANEANDO MATRÍCULA..." : "🔍 SCANNING ACCOUNT...";

            try {
                const res = await fetch(`/api/accounts/owner/${input}`, {
                    headers: { 'Authorization': 'Bearer ' + token }
                });
                if (res.ok) {
                    const ownerName = await res.text();
                    if (ownerName.toUpperCase().includes("DELETED") || ownerName.toUpperCase().includes("INACTIVO")) {
                        preview.style.color = "#e74c3c";
                        preview.innerText = lang === 'es' ? "❌ CUENTA INACTIVA O ELIMINADA" : "❌ INACTIVE OR DELETED ACCOUNT";
                        disableBunkerButton();
                    } else {
                        preview.style.color = "#27ae60";
                        const label = lang === 'es' ? "DESTINATARIO" : "RECIPIENT";
                        preview.innerHTML = `✅ ${label}: <span style="text-transform: uppercase;">${ownerName}</span>`;
                        enableBunkerButton();
                    }
                } else {
                    preview.style.color = "#e74c3c";
                    preview.innerText = lang === 'es' ? "❌ NO HAY DESTINATARIO REGISTRADO" : "❌ NO RECIPIENT REGISTERED";
                    disableBunkerButton();
                }
            } catch (e) {
                preview.innerText = "";
                disableBunkerButton();
            }
        }
        else if (input.length > 0 && input.length < 22) {
            preview.style.color = "#888";
            const remaining = 22 - input.length;
            preview.innerText = lang === 'es' ? `⏳ FALTAN ${remaining} DÍGITOS...` : `⏳ ${remaining} DIGITS REMAINING...`;
            disableBunkerButton("0.7");
        } else {
            preview.innerText = "";
            disableBunkerButton();
        }
    } else {
        preview.innerText = "";
        enableBunkerButton();
    }
}

async function reactivateUser(externalId) {
    const token = localStorage.getItem('jwt_token');
    const lang = localStorage.getItem('user_lang') || 'es';
    const t = translations[lang];
    try {
        const response = await fetch(`/api/users/admin/reactivate/${externalId}`, {
            method: 'PUT',
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (response.ok) {
            const successMsg = lang === 'es'
                ? "🏗️ [SISTEMA]: Bólido resucitado con éxito. Telemetría en verde."
                : "🏗️ [SYSTEM]: Member reactivated successfully. Telemetry is green.";

            showBunkerMessage(successMsg);
            loadAdminDashboard();
        }
        else {
            const contentType = response.headers.get("content-type");
            let rawMsg = "";
            if (contentType && contentType.includes("application/json")) {
                const errorData = await response.json();
                rawMsg = (errorData.error || errorData.message || "").toLowerCase();
            } else {
                rawMsg = (await response.text()).toLowerCase();
            }
            let finalMsg = lang === 'es' ? "REANIMACIÓN FALLIDA" : "REACTIVATION FAILED";
            if (rawMsg.includes("ya está registrado") || rawMsg.includes("already registered") || rawMsg.includes("email")) {
                finalMsg = lang === 'es'
                    ? "🚨 ERROR: Ya existe un usuario activo con este mismo correo electrónico."
                    : "🚨 ERROR: An active member with this email address already exists.";
            }
            showBunkerMessage(finalMsg, true);
        }
    } catch (err) {
        const connectionErr = lang === 'es'
            ? "🚨 ERROR DE CONEXIÓN: El radar no responde."
            : "🚨 CONNECTION ERROR: Radar is offline.";
        showBunkerMessage(connectionErr, true);
    }
}


async function confirmDeleteAccount() {
    const lang = localStorage.getItem('user_lang') || 'es';
    const t = translations[lang];
    const confirmation = confirm(t.delete_confirm);
    if (confirmation) {
        const token = localStorage.getItem('jwt_token');
        const externalId = localStorage.getItem('user_external_id');
        try {
            const response = await fetch(`http://localhost:8080/api/users/${externalId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type': 'application/json'
                }
            });
            if (response.ok) {
                showBunkerMessage(t.delete_success);
                setTimeout(() => {
                    logoutBanco();
                }, 2000);
            } else {
                showBunkerMessage(t.delete_error, true);
            }
        } catch (error) {
            showBunkerMessage(t.delete_conn_error, true);
        }
    }
}
document.addEventListener('DOMContentLoaded', () => {
    const role = localStorage.getItem('user_role');
    if (role && role.includes('ADMIN')) {
        console.log("[ADMIN]: Initiating comprehensive system security scan.");
        loadAdminDashboard();
    }
});
function setLanguage(lang) {
    localStorage.setItem('user_lang', lang);
    updateLanguageUI();
    if (typeof updateRecentMovements === "function") {
        updateRecentMovements();
    }
    const userRole = localStorage.getItem('user_role');
    const isAdmin = userRole && (userRole === 'ADMIN' || userRole === 'ROLE_ADMIN');
    if (isAdmin && typeof loadAdminDashboard === "function") {
        console.log("[SYSTEM]: Synchronizing administrative dashboard management data.");
        loadAdminDashboard();
    }
}

function updateLanguageUI() {
    if (!localStorage.getItem('user_lang')) {
        localStorage.setItem('user_lang', 'es');
    }
    const lang = localStorage.getItem('user_lang');
    const t = translations[lang];
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (t[key]) el.innerText = t[key];
    });
    const userName = localStorage.getItem('user_name');
    const pilotTitle = document.getElementById('pilot-name');
    if (userName && pilotTitle) {
        const welcomeWord = t.welcome || "BIENVENIDO";
        pilotTitle.innerText = `${welcomeWord}, ${userName.toUpperCase()}`;
    }
    if (typeof checkReceiver === "function") {
        checkReceiver();
    }
    document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
        let key = el.getAttribute('data-i18n-placeholder');
        if (el.id === 'receiver') {
            key = (currentTransferMode === 'cvu') ? 'cvu_placeholder' : 'receiver_placeholder';
        }
        if (t[key]) el.placeholder = t[key];
    });
}

async function validateSession() {
    const token = localStorage.getItem('jwt_token');
    if (!token) return;

    try {
        const response = await fetch('/api/accounts/my-balance', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (response.status === 401 || response.status === 403) {
            console.warn("[SECURITY]: Authentication token expired. Initiating emergency session termination.");
            logoutBanco();
            return;
        }
    } catch (error) {
        console.error("[NETWORK]: Critical connection failure with the centralized management gateway.");
    }
}
window.addEventListener('DOMContentLoaded', () => {
    updateLanguageUI();
    validateSession();
});
