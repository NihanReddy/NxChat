// Chat.js - Atmospheric Chat Client

document.addEventListener('DOMContentLoaded', async () => {
// ---- Theme Toggle Logic (Fixed for Tailwind darkMode: "class") ----
  const themeToggle = document.getElementById('themeToggle');
  const html = document.documentElement;
  const isDark = localStorage.getItem('theme') === 'light' ? false : true;
  
  if (isDark) {
    html.classList.add('dark');
    html.setAttribute('data-theme', 'dark');
  } else {
    html.classList.remove('dark');
    html.classList.add('light');
    html.setAttribute('data-theme', 'light');
  }

  if (themeToggle) {
    updateThemeIcon(isDark ? 'dark' : 'light');
    themeToggle.addEventListener('click', () => {
      const newIsDark = !html.classList.contains('dark');
      if (newIsDark) {
        html.classList.add('dark');
        html.classList.remove('light');
        html.setAttribute('data-theme', 'dark');
        localStorage.setItem('theme', 'dark');
        updateThemeIcon('dark');
      } else {
        html.classList.remove('dark');
        html.classList.add('light');
        html.setAttribute('data-theme', 'light');
        localStorage.setItem('theme', 'light');
        updateThemeIcon('light');
      }
    });
  }

  function updateThemeIcon(theme) {
    if (!themeToggle) return;
    const icon = themeToggle.querySelector('span');
    icon.textContent = theme === 'light' ? 'dark_mode' : 'light_mode';
  }

  const token = localStorage.getItem('token');
  const userData = JSON.parse(localStorage.getItem('user') || '{}');
  const currentUser = userData.id ? userData : null;
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  };

  // Emoji Picker (init only when clicked)
  const emojiBtn = document.getElementById('emojiBtn');
  const emojiContainer = document.getElementById('emojiPickerContainer');
  let picker = null;

  emojiBtn?.addEventListener('click', (e) => {
    e.stopPropagation();
    if (!picker) {
      picker = picmo.createPicker({
        rootElement: emojiContainer,
        theme: html.classList.contains('dark') ? 'dark' : 'light'
      });
      picker.addEventListener('emoji:select', (ev) => {
        const input = document.getElementById('messageInput');
        input.value += ev.emoji;
        input.focus();
        hideEmojiPicker();
      });
    }
    
    if (emojiContainer.classList.contains('hidden')) {
      emojiContainer.classList.remove('hidden');
      setTimeout(() => {
        emojiContainer.classList.remove('scale-95', 'opacity-0');
        emojiContainer.classList.add('scale-100', 'opacity-100');
      }, 10);
    } else {
      hideEmojiPicker();
    }
  });

  function hideEmojiPicker() {
    emojiContainer.classList.remove('scale-100', 'opacity-100');
    emojiContainer.classList.add('scale-95', 'opacity-0');
    setTimeout(() => emojiContainer.classList.add('hidden'), 200);
  }

  document.addEventListener('click', (e) => {
    if (picker && !emojiContainer.contains(e.target) && e.target !== emojiBtn) {
      hideEmojiPicker();
    }
  });

  const leftList = document.getElementById('leftList');
  const middleList = document.getElementById('middleList') || document.getElementById('contactList');
  const leftSection = document.querySelector('section:nth-of-type(1)');
  const middleSection = document.getElementById('middleSection');
  
  // Workspace Resizer Logic (Reusing existing middleSection/leftSection)
  const resizer = document.getElementById('resizer');
  let isResizing = false;

  resizer?.addEventListener('mousedown', (e) => {
    isResizing = true;
    document.body.classList.add('select-none', 'cursor-col-resize');
  });

  window.addEventListener('mousemove', (e) => {
    if (!isResizing) return;
    const windowWidth = window.innerWidth;
    let newWidth = e.clientX - middleSection.getBoundingClientRect().left;
    const minWidth = 240; 
    const maxWidthLimit = windowWidth * 0.5;
    
    if (newWidth < minWidth) newWidth = minWidth;
    if (newWidth > maxWidthLimit) newWidth = maxWidthLimit;
    
    const rightSection = document.getElementById('rightSection');
    if (rightSection) {
      const remainingForRight = windowWidth - (middleSection.getBoundingClientRect().left + newWidth);
      if (remainingForRight < windowWidth * 0.4) {
        newWidth = windowWidth - (middleSection.getBoundingClientRect().left + (windowWidth * 0.4));
      }
    }
    
    middleSection.style.width = `${newWidth}px`;
    if (leftSection) leftSection.style.width = `${newWidth}px`;
  });

  window.addEventListener('mouseup', () => {
    if (isResizing) {
      isResizing = false;
      document.body.classList.remove('select-none', 'cursor-col-resize');
    }
  });

  const messages = document.getElementById('messages');
  const messageInput = document.getElementById('messageInput');
  const sendBtn = document.getElementById('sendBtn');
  const plusBtn = document.getElementById('plusBtn');
  const contactSearchInput = document.getElementById('contactSearchInput');
  const middleSearchInput = document.getElementById('middleSearchInput');
  const noContacts = document.getElementById('noContacts');
  const contactsContainer = document.getElementById('contactsContainer') || document.querySelector('#contactsContainer');
  const profileBtn = document.getElementById('profileBtn');
const logoutBtn = document.getElementById('logoutBtn');
    const deleteAccountBtn = document.getElementById('deleteAccountBtn');
  const profileContent = document.getElementById('profileContent');
const toggleContacts = document.getElementById('toggleContacts');
  const toggleRequests = document.getElementById('toggleRequests');
  const requestList = document.getElementById('requestList');
  const requestsContainer = document.getElementById('requestsContainer');
  const globalSearchInput = document.getElementById('globalSearchInput');
  const contactList = document.getElementById('contactList');
  const mobileBottomNav = document.getElementById('mobileBottomNav');
  const mobileTabButtons = mobileBottomNav ? mobileBottomNav.querySelectorAll('button[data-mobile-tab]') : [];
  const groupMembersModal = document.getElementById('groupMembersModal');
  const groupMembersList = document.getElementById('groupMembersList');
  const groupAddCandidates = document.getElementById('groupAddCandidates');
  const groupAdminPanel = document.getElementById('groupAdminPanel');
  const groupMembersSubtitle = document.getElementById('groupMembersSubtitle');
  const deleteGroupBtn = document.getElementById('deleteGroupBtn');
  const pinChatBtn = document.getElementById('pinChatBtn');
  const gifPickerModal = document.getElementById('gifPickerModal');
  const closeGifPickerModal = document.getElementById('closeGifPickerModal');
  const gifSearchInput = document.getElementById('gifSearchInput');
  const gifResults = document.getElementById('gifResults');
  const addContactModal = document.getElementById('addContactModal');
  const addContactBtn = document.getElementById('addContactBtn');
  const closeAddContactModal = document.getElementById('closeAddContactModal');
  const confirmAddContact = document.getElementById('confirmAddContact');
  const addContactEmailInput = document.getElementById('addContactEmailInput');
  const addContactNoContactsBtn = document.getElementById('addContactNoContactsBtn');
  const dismissBanner = document.getElementById('dismissBanner');
  const dmUserDetailsModal = document.getElementById('dmUserDetailsModal');
  const closeDmUserDetailsModal = document.getElementById('closeDmUserDetailsModal');
  const dmUserEmailValue = document.getElementById('dmUserEmailValue');
  const dmUserPhoneValue = document.getElementById('dmUserPhoneValue');
  const copyDmUserEmailBtn = document.getElementById('copyDmUserEmailBtn');
  const copyDmUserPhoneBtn = document.getElementById('copyDmUserPhoneBtn');
  let mobileNavModule = null;
  let profileModule = null;
  let gifModule = null;
  let contactsModule = null;
  let groupModule = null;
  let headerPinModule = null;

  // Sidebar tab functionality
  const sidebarNav = document.querySelector('aside nav');
  const sidebarTabs = sidebarNav ? sidebarNav.querySelectorAll('a[data-tab]') : [];
  const sectionTitleEl = document.querySelector('section h2');
  
  let currentTab = null;
  let currentListType = 'chats';
  let currentFilter = 'all';
  let allContacts = [];
  let currentSearchTerm = '';

  if (!token || !currentUser) {
    window.location.href = 'login.html';
    return;
  }

let recentChats = [];
  let pendingRequests = [];
  let currentChatId = null;
  let currentChatMeta = null;
let stompClient = null;
  let currentSubscription = null;
  let currentUserData = null;

  function loadPinnedDmChats() {
    headerPinModule?.loadPinnedDmChats();
  }

  function isPinnedDm(chat) {
    return headerPinModule ? headerPinModule.isPinnedDm(chat) : false;
  }

  const GIF_PREFIX = '__GIF__:';

  function isGifMessage(content) {
    return gifModule ? gifModule.isGifMessage(content) : (typeof content === 'string' && content.startsWith(GIF_PREFIX));
  }

  function extractGifUrl(content) {
    return gifModule ? gifModule.extractGifUrl(content) : (isGifMessage(content) ? content.slice(GIF_PREFIX.length).trim() : '');
  }

  function isGroupChat(chat) {
    const isGroupFlag = chat?.isGroup;
    const groupFlag = chat?.group;
    const chatType = typeof chat?.chatType === 'string' ? chat.chatType.toUpperCase() : '';

    return (
      isGroupFlag === true || isGroupFlag === 1 || isGroupFlag === 'true' || isGroupFlag === '1' ||
      groupFlag === true || groupFlag === 1 || groupFlag === 'true' || groupFlag === '1' ||
      chatType === 'GROUP'
    );
  }
  
function tabSwitch(tab) {
    if (currentTab === tab) return;
    
    currentTab = tab;
    currentListType = tab === 'contacts' ? 'contacts' : 'chats';
    
    // Update sidebar active states (nav-active class)
    sidebarTabs.forEach(t => {
      t.classList.remove('nav-active', 'text-cyan-300', 'bg-surface-container-highest', 'border-l-4', 'border-cyan-400');
      t.classList.add('text-slate-500');
    });
    const activeTab = sidebarNav.querySelector(`a[data-tab="${tab}"]`);
    if (activeTab) {
      activeTab.classList.add('nav-active', 'text-cyan-300', 'bg-surface-container-highest', 'border-l-4', 'border-cyan-400');
      activeTab.classList.remove('text-slate-500');
    }

    // Sync active state for mobile bottom tabs
    mobileNavModule?.syncTabState(tab);
    
    // Section switching for lists
    if (leftSection && middleSection) {
      if (tab === 'chats' || tab === 'groups') {
        leftSection.classList.add('hidden', 'section-hidden');
        leftSection.classList.remove('section-visible');
        middleSection.classList.remove('hidden', 'section-hidden');
        middleSection.classList.add('section-visible');
      } else if (tab === 'contacts') {
        middleSection.classList.add('hidden', 'section-hidden');
        middleSection.classList.remove('section-visible');
        leftSection.classList.remove('hidden', 'section-hidden');
        leftSection.classList.add('section-visible');
        const contactsSection = document.getElementById('contactsSection');
        if (contactsSection) contactsSection.classList.remove('hidden');
      }

      // On mobile tabs, always return to list panes and hide right chat pane.
      if (window.innerWidth < 768) {
        const rightSection = document.getElementById('rightSection');
        if (rightSection) {
          rightSection.classList.add('hidden');
          rightSection.classList.remove('flex');
        }
      }
    }
    
    // Setup UI listeners for active tab
    if (tab === 'chats' || tab === 'groups') {
      setupFilterListeners(middleSection.querySelector('.p-6'));
      if (middleSearchInput) {
        middleSearchInput.addEventListener('input', debounce(applySearch, 300));
      }
    } else if (tab === 'contacts') {
      setupFilterListeners(leftSection.querySelector('.p-6')); // no filters, but safe
      if (contactSearchInput) {
        contactSearchInput.addEventListener('input', debounce(applySearch, 300));
      }
    }
    
    // Update section title
    const titles = {
      chats: 'Messages',
      groups: 'Groups',
      contacts: 'Broadcasts',
      profile: 'User Profile'
    };
    if (sectionTitleEl) sectionTitleEl.textContent = titles[tab] || 'Messages';
    
    // Load tab data
    switch (tab) {
      case 'chats':
        currentListType = 'chats';
        loadRecentChats(true);
        break;
        case 'groups':
        currentListType = 'groups';
        currentFilter = 'group';
        loadRecentChats(true);
        break;  
      case 'contacts':
        loadContacts();
        loadPendingRequests();
        if (requestsContainer) {
          requestsContainer.classList.remove('max-h-0');
          requestsContainer.classList.add('h-[30vh]');
          const reqIcon = document.querySelector('#toggleRequests span.material-symbols-outlined');
          if (reqIcon) reqIcon.textContent = 'expand_less';
        }
        if (contactsContainer) {
          contactsContainer.classList.remove('max-h-0');
          contactsContainer.classList.add('h-[60vh]');
          const conIcon = document.querySelector('#toggleContacts span.material-symbols-outlined');
          if (conIcon) conIcon.textContent = 'expand_less';
        }
        break;
      case 'profile':
        loadCurrentUser();
        const profileModal = document.getElementById('profileModal');
        if (profileModal) profileModal.classList.remove('hidden');
        break;
    }
  }
  async function loadCurrentUser() {
    if (!profileModule) return;
    await profileModule.loadCurrentUser();
  }

  function renderProfile() {
    if (!profileModule) return;
    profileModule.renderProfile();
  }

  async function connectWebSocket() {
    if (stompClient && stompClient.connected) return;

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({ Authorization: `Bearer ${token}` }, () => {
      console.log('WebSocket connected');
      loadPendingRequests(); // Bootstrap state from atmosphere
      stompClient.subscribe('/user/queue/contacts', (msg) => {
        try {
          const payload = JSON.parse(msg.body);
          console.log('[WS] Contact event:', payload.type, payload);

          if (payload.type === 'REQUEST_RECEIVED') {
            const name = payload.senderUsername || payload.senderEmail || 'Someone';
            // Refresh requests FIRST, then notify
            loadPendingRequests().then(() => {
              console.log('Pending requests refreshed after REQUEST_RECEIVED');
            });
            showToast(`📡 New handshake request from ${name}`);
          } else if (payload.type === 'REQUEST_ACCEPTED' || payload.type === 'REQUEST_ACCEPTED_SELF') {
            const name = payload.senderUsername || payload.senderEmail || 'a user';
            showToast(`✅ Handshake with ${name} accepted! DM chat is live.`);
            loadContacts();
            loadRecentChats(true);
            loadPendingRequests();
          } else if (payload.type === 'REQUEST_DENIED') {
            showToast('❌ Handshake request was denied.');
            loadPendingRequests();
          }
        } catch (err) {
          console.error('contact socket parse error', err);
        }
      });

      if (currentChatId) {
        subscribeToChat(currentChatId);
      }
    }, (error) => {
      console.error('WebSocket connection error', error);
    });
  }

  function subscribeToChat(chatId) {
    if (!stompClient || !stompClient.connected) return;

    if (currentSubscription) {
      try {
        currentSubscription.unsubscribe();
      } catch (err) {
        // ignore
      }
      currentSubscription = null;
    }

    currentSubscription = stompClient.subscribe(`/topic/chat/${chatId}`, (message) => {
      try {
        const payload = JSON.parse(message.body);
        if (payload.type === 'TYPING' || payload.type === 'READ_RECEIPT') {
          return;
        }

        if (payload.content) {
          appendMessage(payload, payload.sender?.id === currentUser.id);
          autoScroll();
        }
      } catch (err) {
        console.error('WS message parsing error', err);
      }
    });
  }

function getFilteredData() {
  let data = [];
  if (currentListType === 'contacts') {
    data = allContacts.filter(contact => 
      contact.username?.toLowerCase().includes(currentSearchTerm.toLowerCase()) ||
      contact.email?.toLowerCase().includes(currentSearchTerm.toLowerCase())
    );
  } else {
    // chats/groups
    data = recentChats.filter(chat => 
      chat.recipientName?.toLowerCase().includes(currentSearchTerm.toLowerCase()) ||
      chat.lastMessageContent?.toLowerCase().includes(currentSearchTerm.toLowerCase())
    );
    if (currentListType === 'groups' || currentFilter === 'group') {
      data = data.filter(c => isGroupChat(c));
    } else if (currentFilter === 'unread') {
      data = data.filter(c => (c.unreadCount || 0) > 0);
    }
  }

  if (currentListType === 'chats' && currentFilter !== 'group') {
    data = [...data].sort((a, b) => {
      const aPinned = isPinnedDm(a) ? 1 : 0;
      const bPinned = isPinnedDm(b) ? 1 : 0;
      if (aPinned !== bPinned) return bPinned - aPinned;
      const aTime = a?.timestamp ? new Date(a.timestamp).getTime() : 0;
      const bTime = b?.timestamp ? new Date(b.timestamp).getTime() : 0;
      return bTime - aTime;
    });
  }
  return data;
}

function renderCurrentList() {
  const data = getFilteredData();
  const listEl = currentListType === 'contacts' ? leftList : middleList;
  
  if (!data.length) {
    const noDataMsg = currentListType === 'contacts' 
      ? 'No contacts match your search' 
      : 'No chats match your search/filter';
    listEl.innerHTML = `<div class="py-12 text-center text-on-surface-variant">${noDataMsg}</div>`;
    return;
  }

  if (currentListType === 'contacts') {
    renderContacts(listEl, data);
  } else {
    renderChats(listEl, data);
  }
}

function renderChats(listEl, chatsData) {
  if (listEl) {
    listEl.innerHTML = chatsData.map(chat => {
      const timestamp = chat.timestamp ? new Date(chat.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '—';
      const unreadBadge = (chat.unreadCount || 0) > 0 ? `<span class="text-xs text-white bg-error px-2 py-1 rounded-full">${chat.unreadCount}</span>` : '';
      const isDeleted = chat.recipientDeleted === true || chat.recipientStatus === 'DELETED';
      const displayName = isDeleted ? 'Deleted Account' : (chat.recipientName || 'Unknown');
      const avatarUrl = chat.recipientAvatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(displayName)}&background=8ff5ff&color=001115`;
      const statusColor = isDeleted ? 'bg-error' : (chat.recipientStatus === 'ONLINE' ? 'bg-tertiary-dim' : 'bg-on-surface-variant/40');
      const messagePreview = chat.lastMessageContent || 'No transmissions yet';
      const pinnedMark = isPinnedDm(chat) ? '<span class="material-symbols-outlined text-[14px] text-primary" title="Pinned">keep</span>' : '';
      
      return `
        <div class="flex items-center gap-4 p-4 rounded-xl hover:bg-white/5 transition-all cursor-pointer ${currentChatId === chat.chatId ? 'bg-surface-container-highest border-l-4 border-secondary' : ''}" data-chat-id="${chat.chatId}">
          <div class="relative shrink-0">
            <div class="w-12 h-12 rounded-full overflow-hidden" style="background-image:url('${avatarUrl}'); background-size:cover;"></div>
            <span class="absolute bottom-0 right-0 w-3 h-3 ${statusColor} border-2 border-surface-container-low rounded-full"></span>
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex justify-between items-center mb-0.5">
              <span class="font-bold text-on-surface truncate flex items-center gap-1.5">${pinnedMark}${displayName}</span>
              <span class="text-[10px] text-on-surface-variant">${timestamp}</span>
            </div>
            <p class="text-sm text-on-surface-variant truncate font-light">${messagePreview}</p>
          </div>
          ${unreadBadge}
        </div>
      `;
    }).join('');

    // Add click handlers
    listEl.querySelectorAll('div[data-chat-id]').forEach(item => {
      item.addEventListener('click', () => {
        const chatId = Number(item.getAttribute('data-chat-id'));
        const chatData = chatsData.find(c => c.chatId === chatId);
        selectChat(chatId, chatData);
      });
    });
  }
}

function renderContacts(listEl, contactsData) {
  if (listEl) {
    if (!contactsData.length) {
      listEl.innerHTML = '<div class="py-12 text-center text-on-surface-variant">No signal detected in the grid</div>';
      return;
    }
    
    listEl.innerHTML = contactsData.map(contact => {
      const avatarUrl = contact.profileImageUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(contact.username || contact.name || contact.email || 'User')}&background=8ff5ff&color=001115`;
      const statusText = contact.status || 'OFFLINE';
      const statusColor = statusText === 'ONLINE' ? 'bg-cyan-500 shadow-[0_0_8px_rgba(6,182,212,0.6)]' : 'bg-slate-500/50';
      
      return `
        <div class="flex items-center gap-4 p-4 rounded-xl hover:bg-white/5 transition-all cursor-pointer group" data-user-id="${contact.id}">
          <div class="relative shrink-0">
            <div class="w-12 h-12 rounded-full overflow-hidden border border-white/5 group-hover:border-primary/30 transition-all" style="background-image:url('${avatarUrl}'); background-size:cover; background-position:center;"></div>
            <span class="absolute bottom-0.5 right-0.5 w-3 h-3 ${statusColor} border-2 border-slate-900 rounded-full"></span>
          </div>
          <div class="flex-1 min-w-0">
            <div class="font-bold text-on-surface truncate group-hover:text-primary transition-colors">${contact.username || contact.name || contact.email}</div>
            <div class="flex items-center gap-1.5 mt-0.5">
              <span class="text-[10px] text-on-surface-variant font-label uppercase tracking-widest opacity-60">${statusText}</span>
            </div>
          </div>
        </div>
      `;
    }).join('');

    // Contact click handlers
    listEl.querySelectorAll('div[data-user-id]').forEach(item => {
      item.addEventListener('click', async () => {
        const userId = Number(item.getAttribute('data-user-id'));
        try {
          const res = await fetch(`/api/chats/private?userId=${userId}`, { headers });
          if (res.ok) {
            const data = await res.json();
            const chatId = data.chatId || data.id;
            await loadRecentChats(true);
            const contact = contactsData.find(u => u.id === userId);
            selectChat(chatId, {
                recipientName: contact.username || contact.email,
                recipientAvatarUrl: contact.profileImageUrl,
                recipientStatus: contact.status
            });
            tabSwitch('chats');
          }
        } catch (err) {
          console.error('Create chat error:', err);
        }
      });
    });
  }
}

function applyFilters() {
  renderCurrentList();
}

function applySearch(e) {
  currentSearchTerm = e.target.value.trim();
  renderCurrentList();
}

async function loadRecentChats(toMiddle = false) {
    const listEl = toMiddle ? middleList : leftList;
    if (listEl) {
      listEl.innerHTML = '<div class="flex items-center justify-center py-12"><div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div><span class="ml-2 text-on-surface-variant">Loading chats...</span></div>';
    }
    try {
      console.log('Fetching recent chats...');
      const res = await fetch('/api/chats/recent', { headers });
      if (!res.ok) throw new Error(`HTTP ${res.status}: Unable to fetch recent chats`);
      recentChats = await res.json();
      console.log('Recent chats loaded:', recentChats.length);
      renderCurrentList();

      // Auto-select first chat if none selected and not already in one
      if (!currentChatId && recentChats.length > 0) {
        const first = recentChats[0];
        console.log('Auto-selecting first chat:', first.chatId);
        selectChat(first.chatId, first);
      } else if (currentChatId) {
        // Just refresh the header highlight if already selected
        highlightSelectedChat(currentChatId);
      }
    } catch (err) {
      console.error('loadRecentChats error:', err);
      if (listEl) listEl.innerHTML = '<div class="py-12 text-center text-on-surface-variant"><p>Could not load chats.</p><p class="text-xs mt-2 opacity-75">Connect to the grid and try again.</p></div>';
    }
  }

  async function loadMyGroups() {
    try {
      const res = await fetch('/api/chats/recent', { headers });
      if (!res.ok) throw new Error('Unable to fetch chats');
      recentChats = await res.json();
      renderCurrentList();
    } catch (err) {
      console.error('loadMyGroups', err);
      middleList.innerHTML = '<div class="py-12 text-center text-on-surface-variant">No groups found.</div>';
    }
  }

  async function loadContacts() {
    if (!contactsModule) return;
    await contactsModule.loadContacts();
  }

// Consolidated into renderContacts(listEl, data) above

async function loadPendingRequests() {
    if (!contactsModule) return;
    await contactsModule.loadPendingRequests();
  }

function updatePendingBadge(count) {
    contactsModule?.updatePendingBadge(count);
  }

  function renderRequests() {
    contactsModule?.renderRequests();
  }

  async function loadChatMessages(chatId) {
    const messagesEl = document.getElementById('messages');
    if (!messagesEl) {
        console.error('Messages container #messages not found in the DOM.');
        return;
    }

    if (!chatId) {
      console.warn('loadChatMessages called with null/empty chatId');
      messagesEl.innerHTML = '<div class="flex items-center justify-center h-full py-12 text-on-surface-variant">Initialize a link to view encrypted data</div>';
      return;
    }

    messagesEl.innerHTML = '<div class="flex items-center justify-center h-full py-12 text-on-surface-variant"><div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mr-2"></div>Loading messages...</div>';
    try {
      console.log(`[Link] Fetching transmissions for channel: ${chatId}...`);
      const res = await fetch(`/api/chats/${chatId}/messages`, { headers });
      if (!res.ok) {
        const body = await res.text();
        throw new Error(`HTTP ${res.status}: ${body || 'Unable to fetch messages'}`);
      }
      const data = await res.json();
      console.log(`[Link] messages loaded.`, data);
      
      if (!Array.isArray(data)) {
        throw new Error('Unexpected response format from server');
      }

      messagesEl.innerHTML = '';
      if (data.length === 0) {
        messagesEl.innerHTML = `
          <div class="flex flex-col items-center justify-center h-full text-on-surface-variant/30 italic font-light py-20">
            <p class="text-lg">No previous transmissions.</p>
            <p class="text-xs mt-1 opacity-60">Start the conversation below.</p>
          </div>
        `;
      } else {
        data.forEach(msg => appendMessage(msg, msg.sender?.id === currentUser.id));
      }
      autoScroll();
      setTimeout(autoScroll, 100);
      
      // Ensure websocket is connected for live updates
      await connectWebSocket();
      subscribeToChat(chatId);
    } catch (err) {
      console.error('loadChatMessages error:', err);
      showMessageLoadError(chatId, err);
      // optionally auto retry once for freshness
      if (chatId && !window.$$retryingChatId) {
        window.$$retryingChatId = chatId;
        setTimeout(() => {
          window.$$retryingChatId = null;
          if (currentChatId === chatId) loadChatMessages(chatId);
        }, 1200);
      }
    }
  }

  function showMessageLoadError(chatId, err) {
    const messagesEl = document.getElementById('messages');
    if (!messagesEl) return;
    const details = err?.message || 'Unable to load messages';
    messagesEl.innerHTML = `
      <div class="flex-grow flex flex-col items-center justify-center py-12 text-center text-on-surface-variant">
        <span class="material-symbols-outlined text-4xl mb-4 text-error">warning</span>
        <p class="text-lg mb-2 font-bold text-error">System Error</p>
        <p class="text-xs opacity-75 mb-6 max-w-sm">${escapeHtml(details)}</p>
        <button id="retryMessagesBtn" class="bg-primary hover:bg-primary-container text-on-primary-fixed hover:text-on-primary-container font-bold px-6 py-2.5 rounded-full transition-all shadow-lg hover:shadow-primary/20">Retry Connection</button>
      </div>
    `;
    const retryBtn = document.getElementById('retryMessagesBtn');
    if (retryBtn) {
      retryBtn.addEventListener('click', () => {
        loadChatMessages(chatId);
      });
    }
  }

  function appendMessage(msg, isOutgoing) {
    const messagesEl = document.getElementById('messages');
    if (!messagesEl) return;

    const senderDeleted = !isOutgoing && msg?.sender?.deleted === true;

    const wrapper = document.createElement('div');
    wrapper.className = `flex flex-col max-w-[85%] mb-4 ${isOutgoing ? 'items-end self-end ml-12' : 'items-start self-start mr-12'}`;

    const bubble = document.createElement('div');
    // Using Tailwind semantic tokens for dynamic mode switching
    bubble.className = `${isOutgoing ? 'bg-primary text-on-primary rounded-3xl rounded-tr-none' : 'bg-surface-container-low dark:bg-slate-700 text-on-surface dark:text-slate-50 rounded-3xl rounded-tl-none'} px-6 py-3.5 shadow-xl border border-outline-variant/10 transition-transform hover:scale-[1.01]`;
    const gifUrl = extractGifUrl(msg.content);
    if (gifUrl) {
      bubble.innerHTML = `
        ${senderDeleted ? '<div class="mb-2 inline-flex items-center rounded-full bg-error/15 border border-error/40 text-error text-[10px] uppercase tracking-wider font-bold px-2.5 py-1">This account was deleted</div>' : ''}
        <div class="mb-2 text-[10px] uppercase tracking-widest opacity-70 font-bold">GIF</div>
        <img src="${escapeHtml(gifUrl)}" alt="GIF" class="rounded-2xl max-w-[240px] md:max-w-[300px] object-cover" loading="lazy" />
      `;
    } else {
      bubble.innerHTML = `
        ${senderDeleted ? '<div class="mb-2 inline-flex items-center rounded-full bg-error/15 border border-error/40 text-error text-[10px] uppercase tracking-wider font-bold px-2.5 py-1">This account was deleted</div>' : ''}
        <p class="text-[15px] leading-relaxed break-words whitespace-pre-wrap font-medium">${escapeHtml(msg.content)}</p>
      `;
    }

    wrapper.appendChild(bubble);

    const time = document.createElement('span');
    time.className = 'mt-2 text-[10px] text-on-surface-variant font-label';
    time.textContent = msg.createdAt ? new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Just now';
    wrapper.appendChild(time);

    messagesEl.appendChild(wrapper);
  }

  function escapeHtml(text) {
    if (text == null) return '';
    return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function autoScroll() {
    if (!messages) return;
    messages.scrollTop = messages.scrollHeight;
  }

  async function searchUsers(query) {
    if (!query) return;

    try {
      const res = await fetch(`/api/users/search?query=${encodeURIComponent(query)}`, { headers });
      if (!res.ok) throw new Error('Search failed');
      const users = await res.json();

      if (!users.length) {
        contactList.innerHTML = '<div class="p-8 text-center text-on-surface-variant/40 italic text-sm">No signal detected in current radius</div>';
        contactList.classList.add('hidden', 'scale-95', 'opacity-0');
        return;
      }

      contactList.classList.remove('hidden');
      setTimeout(() => contactList.classList.remove('scale-95', 'opacity-0'), 10);

      contactList.innerHTML = users.map(user => {
        const label = user.name || user.username || user.email;
        return `
          <div class="flex items-center justify-between p-4 mx-2 my-1 rounded-xl hover:bg-white/5 transition-all group cursor-pointer" data-user-id="${user.id}">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-2xl overflow-hidden border border-white/10 shadow-lg" style="background-image:url('https://ui-avatars.com/api/?name=${encodeURIComponent(label)}&background=8ff5ff&color=001115'); background-size:cover;"></div>
              <div class="min-w-0">
                <p class="font-bold text-on-surface truncate">${label}</p>
                <p class="text-[10px] text-on-surface-variant/60 truncate uppercase tracking-widest">${user.email}</p>
              </div>
            </div>
            <button class="w-10 h-10 rounded-full flex items-center justify-center bg-primary/10 text-primary hover:bg-primary hover:text-on-primary-fixed transition-all shadow-lg group-hover:scale-110">
              <span class="material-symbols-outlined text-xl">link</span>
            </button>
          </div>
        `;
      }).join('');

      document.querySelectorAll('#contactList > div').forEach(item => {
        item.addEventListener('click', async () => {
          const userId = Number(item.getAttribute('data-user-id'));
          if (!userId) return;

          const createRes = await fetch(`/api/chats/private?userId=${userId}`, { headers });
          if (!createRes.ok) throw new Error('Could not start private chat');

          const payload = await createRes.json();
          const newChatId = payload.chatId || payload.id;

          if (newChatId) {
            await loadRecentChats();
            selectChat(newChatId);
          }
        });
      });
    } catch (err) {
      console.error('searchUsers', err);
    }
  }

  function selectChat(chatId, chatData) {
    console.log('Selecting chat:', chatId);
    currentChatId = chatId;
    currentChatMeta = chatData || recentChats.find(c => c.chatId === chatId) || null;
    // Update header if chatData provided
    if (currentChatMeta) {
      updateChatHeader(currentChatMeta);
    } else {
        // Find in recentChats
        const found = recentChats.find(c => c.chatId === chatId);
        if (found) {
        currentChatMeta = found;
            updateChatHeader(found);
        } else {
            console.warn('Chat data not found for header update', chatId);
        }
    }

    highlightSelectedChat(chatId);
    loadChatMessages(chatId);
    
    // Clear notification UI immediately
    const chatInList = recentChats.find(c => c.chatId === chatId);
    if (chatInList) {
        chatInList.unreadCount = 0;
        const selector = `#middleList div[data-chat-id="${chatId}"] .unread-badge, #leftList div[data-chat-id="${chatId}"] .unread-badge, div[data-chat-id="${chatId}"] .unread-count`;
        document.querySelectorAll(selector).forEach(el => el.classList.add('hidden'));
    }

    // Persist read state to backend
    fetch(`/api/chats/${chatId}/read`, { method: 'POST', headers }).catch(e => console.warn('Persistence leak during read sync', e));

    // Mobile: open the chat pane and hide list panes.
    if (window.innerWidth < 768) {
      const rightSection = document.getElementById('rightSection');
      const contactsSection = document.getElementById('contactsSection');
      const middleSectionEl = document.getElementById('middleSection');
      if (contactsSection) {
        contactsSection.classList.add('hidden');
        contactsSection.classList.remove('flex');
      }
      if (middleSectionEl) {
        middleSectionEl.classList.add('hidden');
        middleSectionEl.classList.remove('flex');
      }
      if (rightSection) {
        rightSection.classList.remove('hidden');
        rightSection.classList.add('flex');
      }
    }
  }

  function highlightSelectedChat(chatId) {
    // Highlight in both lists
    document.querySelectorAll('#leftList div[data-chat-id], #middleList div[data-chat-id]').forEach(el => {
        el.classList.remove('bg-white/10', 'border-l-4', 'border-primary');
    });
    const selectedEl = document.querySelector(`#middleList div[data-chat-id="${chatId}"], #leftList div[data-chat-id="${chatId}"]`);
    if (selectedEl) {
      selectedEl.classList.add('bg-white/10', 'border-l-4', 'border-primary');
    }
  }

  function updateChatHeader(chat) {
    headerPinModule?.updateChatHeader(chat);
  }

  function togglePinCurrentChat() {
    headerPinModule?.togglePinCurrentChat();
  }

  async function openGroupMembersModal() {
    if (!groupModule) return;
    await groupModule.openGroupMembersModal();
  }

  function renderGroupMembersList(members, isAdmin) {
    // Group rendering is managed inside group module.
  }

  async function renderAddCandidatesForGroup(members) {
    // Group candidate rendering is managed inside group module.
  }

  async function copyToClipboard(value, label) {
    if (!value) {
      showToast(`⚠️ ${label} not available.`);
      return;
    }

    try {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        await navigator.clipboard.writeText(value);
      } else {
        const helper = document.createElement('textarea');
        helper.value = value;
        helper.style.position = 'fixed';
        helper.style.opacity = '0';
        document.body.appendChild(helper);
        helper.focus();
        helper.select();
        document.execCommand('copy');
        document.body.removeChild(helper);
      }
      showToast(`✅ ${label} copied.`);
    } catch (err) {
      console.error('Copy failed:', err);
      showToast(`❌ Failed to copy ${label.toLowerCase()}.`);
    }
  }

  function closeDmDetailsModal() {
    dmUserDetailsModal?.classList.add('hidden');
  }

  function openDmDetailsModal() {
    if (!currentChatMeta || isGroupChat(currentChatMeta)) return;

    const email = (currentChatMeta.recipientEmail || '').trim();
    const phone = (currentChatMeta.recipientPhone || '').trim();

    if (dmUserEmailValue) {
      dmUserEmailValue.value = email || 'Not available';
    }
    if (dmUserPhoneValue) {
      dmUserPhoneValue.value = phone || 'Not available';
    }
    if (copyDmUserEmailBtn) {
      copyDmUserEmailBtn.disabled = !email;
      copyDmUserEmailBtn.classList.toggle('opacity-50', !email);
      copyDmUserEmailBtn.classList.toggle('cursor-not-allowed', !email);
    }
    if (copyDmUserPhoneBtn) {
      copyDmUserPhoneBtn.disabled = !phone;
      copyDmUserPhoneBtn.classList.toggle('opacity-50', !phone);
      copyDmUserPhoneBtn.classList.toggle('cursor-not-allowed', !phone);
    }

    dmUserDetailsModal?.classList.remove('hidden');
  }

  async function handleDeleteAccount() {
    const confirmed = confirm('Delete your account permanently? Your DM contacts will see "Deleted Account" for your profile.');
    if (!confirmed) return;

    try {
      const res = await fetch('/api/users/me', { method: 'DELETE', headers });
      if (!res.ok) {
        const message = await res.text();
        throw new Error(message || `Delete failed with HTTP ${res.status}`);
      }
      localStorage.clear();
      window.location.href = 'login.html';
    } catch (err) {
      console.error('Delete account error:', err);
      showToast(`❌ ${err.message || 'Failed to delete account. Please try again.'}`);
    }
  }

  async function handleDeleteGroup() {
    if (!groupModule) return;
    await groupModule.handleDeleteGroup();
  }

  function openGifModal() {
    gifModule?.openGifModal();
  }

  function closeGifModal() {
    gifModule?.closeGifModal();
  }

  async function loadGifResults(query) {
    if (!gifModule) return;
    await gifModule.loadGifResults(query);
  }

  async function sendChatContent(content) {
    const text = (content || '').trim();
    if (!text || !currentChatId) return;

    if (stompClient && stompClient.connected) {
      stompClient.send('/app/chat.send', {}, JSON.stringify({ chatId: currentChatId, content: text }));
      return;
    }

    await fetch(`/api/chats/${currentChatId}/messages`, {
      method: 'POST',
      headers,
      body: JSON.stringify({ content: text })
    });

    await loadChatMessages(currentChatId);
  }

  sendBtn?.addEventListener('click', async () => {
    const text = messageInput.value.trim();
    if (!text || !currentChatId) return;
    try {
      await sendChatContent(text);
      messageInput.value = '';
    } catch (err) {
      console.error('sendMessage', err);
    }
  });

  messageInput?.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      sendBtn.click();
    }
  });

  // Search handlers will be set up after tabSwitch

  function debounce(fn, ms) {
    let timeout;
    return (...args) => {
      clearTimeout(timeout);
      timeout = setTimeout(() => fn(...args), ms);
    };
  }

  /**
   * showToast — lightweight non-blocking notification replacing alert() calls.
   * Auto-dismisses after `duration` ms. Stacks vertically from bottom-right.
   */
  function showToast(message, duration = 4000) {
    let container = document.getElementById('toastContainer');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toastContainer';
      container.style.cssText = `
        position: fixed; bottom: 24px; right: 24px; z-index: 9999;
        display: flex; flex-direction: column-reverse; gap: 8px;
        max-width: 340px; pointer-events: none;
      `;
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.style.cssText = `
      background: rgba(0,42,50,0.95); color: #b7eefc;
      border: 1px solid rgba(143,245,255,0.2); border-radius: 12px;
      padding: 12px 18px; font-size: 13px; font-family: Manrope, sans-serif;
      backdrop-filter: blur(16px); pointer-events: auto;
      box-shadow: 0 8px 32px rgba(0,0,0,0.4);
      animation: toastIn 0.25s ease; opacity: 1; transition: opacity 0.3s ease;
    `;
    toast.textContent = message;

    if (!document.getElementById('toastStyle')) {
      const style = document.createElement('style');
      style.id = 'toastStyle';
      style.textContent = `@keyframes toastIn { from { opacity:0; transform:translateY(12px); } to { opacity:1; transform:translateY(0); } }`;
      document.head.appendChild(style);
    }

    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      setTimeout(() => toast.remove(), 300);
    }, duration);
  }

  mobileNavModule = window.NxMobileNav?.create({
    mobileBottomNav,
    mobileTabButtons,
    tabSwitch,
    getCurrentTab: () => currentTab
  }) || null;

  profileModule = window.NxProfileModule?.create({
    headers,
    profileContent,
    showToast,
    escapeHtml,
    getCurrentUserData: () => currentUserData,
    setCurrentUserData: (nextUser) => {
      currentUserData = nextUser;
    },
    onProfileUpdated: () => {
      if (currentChatMeta) {
        updateChatHeader(currentChatMeta);
      }
    }
  }) || null;

  gifModule = window.NxGifModule?.create({
    gifPrefix: GIF_PREFIX,
    gifPickerModal,
    closeGifPickerModal,
    gifSearchInput,
    gifResults,
    plusBtn,
    sendChatContent,
    escapeHtml,
    debounce
  }) || null;

  contactsModule = window.NxContactsModule?.create({
    headers,
    showToast,
    renderCurrentList,
    getCurrentTab: () => currentTab,
    setAllContacts: (nextContacts) => {
      allContacts = nextContacts;
    },
    getPendingRequests: () => pendingRequests,
    setPendingRequests: (nextRequests) => {
      pendingRequests = nextRequests;
    },
    loadRecentChats,
    tabSwitch,
    selectChat,
    requestList,
    requestsContainer,
    addContactModal,
    addContactBtn,
    addContactNoContactsBtn,
    closeAddContactModal,
    confirmAddContact,
    addContactEmailInput,
    toggleContacts,
    contactsContainer,
    toggleRequests,
    dismissBanner
  }) || null;

  groupModule = window.NxGroupModule?.create({
    headers,
    showToast,
    escapeHtml,
    isGroupChat,
    currentUser,
    loadRecentChats,
    loadContacts,
    getAllContacts: () => allContacts,
    getCurrentChatId: () => currentChatId,
    getCurrentChatMeta: () => currentChatMeta,
    clearCurrentChat: () => {
      currentChatId = null;
      currentChatMeta = null;
    },
    groupMembersModal,
    groupMembersList,
    groupAddCandidates,
    groupAdminPanel,
    groupMembersSubtitle,
    deleteGroupBtn,
    onOpenDmDetails: openDmDetailsModal
  }) || null;

  headerPinModule = window.NxHeaderPinModule?.create({
    currentUser,
    pinChatBtn,
    isGroupChat,
    showToast,
    renderCurrentList,
    getCurrentChatId: () => currentChatId,
    getCurrentChatMeta: () => currentChatMeta
  }) || null;

  mobileNavModule?.bindEvents();
  gifModule?.bindEvents();
  contactsModule?.bindWindowActions();
  contactsModule?.bindPanelEvents();
  contactsModule?.bindAddContactEvents();
  groupModule?.bindEvents();
  headerPinModule?.bindEvents();

  // Sidebar tab click handlers
  sidebarNav?.addEventListener('click', (e) => {
    const tabLink = e.target.closest('a[data-tab]');
    if (tabLink) {
      e.preventDefault();
      tabSwitch(tabLink.dataset.tab);
    }
  });

  profileBtn?.addEventListener('click', async (e) => {
    e.preventDefault();
    tabSwitch('profile');
  });
  logoutBtn?.addEventListener('click', async () => { localStorage.clear(); window.location.href = 'login.html'; });
  
  document.getElementById('logoutActionBtn')?.addEventListener('click', (e) => {
    e.preventDefault();
    localStorage.clear();
    window.location.href = 'login.html';
  });

  deleteAccountBtn?.addEventListener('click', handleDeleteAccount);
  document.getElementById('deleteAccountActionBtn')?.addEventListener('click', (e) => {
    e.preventDefault();
    handleDeleteAccount();
  });

  // Modal close buttons
  document.getElementById('closeProfileModal')?.addEventListener('click', () => {
    document.getElementById('profileModal')?.classList.add('hidden');
  });
  document.getElementById('closeAddContactModal')?.addEventListener('click', () => {
    document.getElementById('addContactModal')?.classList.add('hidden');
  });
  closeDmUserDetailsModal?.addEventListener('click', closeDmDetailsModal);
  dmUserDetailsModal?.addEventListener('click', (e) => {
    if (e.target === dmUserDetailsModal) {
      closeDmDetailsModal();
    }
  });
  copyDmUserEmailBtn?.addEventListener('click', () => {
    copyToClipboard((currentChatMeta?.recipientEmail || '').trim(), 'Email');
  });
  copyDmUserPhoneBtn?.addEventListener('click', () => {
    copyToClipboard((currentChatMeta?.recipientPhone || '').trim(), 'Mobile');
  });
  // Sync Contacts with Google Logic
  const syncBtn = document.getElementById('syncContactsBtn');
  if (syncBtn && window.google) {
    const client = google.accounts.oauth2.initTokenClient({
      client_id: 'YOUR_GOOGLE_CLIENT_ID', // Replace with production ID
      scope: 'https://www.googleapis.com/auth/contacts.readonly',
      callback: async (response) => {
        if (response && response.access_token) {
          try {
            syncBtn.querySelector('.sidebar-text').innerHTML = 'Syncing...';
            const res = await fetch('/api/contacts/sync-google', {
              method: 'POST',
              headers,
              body: JSON.stringify({ accessToken: response.access_token })
            });
            const data = await res.json();
            if (res.ok) {
              alert(data.message || 'Synced contacts successfully!');
              await loadPendingRequests();
            } else {
              alert(data.message || 'Sync failed.');
            }
          } catch (err) {
            console.error('Sync error:', err);
            alert('Failed to connect to NxChat Sync server.');
          } finally {
            syncBtn.querySelector('.sidebar-text').innerHTML = 'Sync Contacts';
          }
        }
      }
    });

    syncBtn.addEventListener('click', () => {
      client.requestAccessToken();
    });
  }

  // Desktop Sidebar Collapse Logic
  const collapseBtn = document.getElementById('collapseSidebarBtn');
  const desktopSidebar = document.getElementById('desktopSidebar');
  const logoText = document.getElementById('sidebarLogoText');
  let isCollapsed = false;

  collapseBtn?.addEventListener('click', () => {
    isCollapsed = !isCollapsed;
    if (isCollapsed) {
      desktopSidebar.classList.remove('w-72');
      desktopSidebar.classList.add('w-24');
      logoText.classList.add('hidden');
      collapseBtn.querySelector('span').textContent = 'menu';
      document.querySelectorAll('.sidebar-text').forEach(el => el.classList.add('hidden'));
    } else {
      desktopSidebar.classList.remove('w-24');
      desktopSidebar.classList.add('w-72');
      logoText.classList.remove('hidden');
      collapseBtn.querySelector('span').textContent = 'menu_open';
      document.querySelectorAll('.sidebar-text').forEach(el => el.classList.remove('hidden'));
    }
  });

  // Allow intercepting the sidebar collapse click on mobile to hide the whole sidebar 
  if (collapseBtn) {
    collapseBtn.addEventListener('click', (e) => {
      if (window.innerWidth < 768) {
        desktopSidebar.classList.add('hidden');
        desktopSidebar.classList.remove('fixed', 'inset-0', 'z-50', 'w-full', 'bg-surface/95');
      }
    });
  }

  // Filters logic - tab aware
  function setupFilterListeners(container) {
    const filterBtns = container.querySelectorAll('button[data-filter]');
    filterBtns.forEach(btn => {
      btn.addEventListener('click', (e) => {
        // Update active state
        filterBtns.forEach(b => {
          b.classList.remove('bg-primary', 'text-white', 'filter-active');
          b.classList.add('border', 'border-outline-variant', 'text-on-surface-variant');
        });
        const target = e.currentTarget;
        target.classList.remove('border', 'border-outline-variant', 'text-on-surface-variant');
        target.classList.add('bg-primary', 'text-white', 'filter-active');
        
        currentFilter = target.dataset.filter;
        if (currentListType === 'groups') currentFilter = 'group'; // Force group filter on groups tab
        applyFilters();
      });
    });
  }

  globalSearchInput?.addEventListener('input', debounce((e) => {
    const val = e.target.value.trim();
    if (!val) {
      contactList.classList.add('hidden', 'scale-95', 'opacity-0');
      return;
    }
    searchUsers(val);
  }, 400));

  document.getElementById('closeAddContactModal')?.addEventListener('click', () => {
    document.getElementById('addContactModal').classList.add('hidden');
    globalSearchInput.value = '';
    contactList.classList.add('hidden', 'scale-95', 'opacity-0');
  });

  const deleteBtn = document.getElementById('deleteChatBtn');
  deleteBtn?.addEventListener('click', async () => {
    if (!currentChatId) return;
    if (!confirm('Permanently purge this conversation history? This cannot be undone.')) return;

    try {
      const res = await fetch(`/api/chats/${currentChatId}`, { method: 'DELETE', headers });
      if (res.ok) {
        showToast('✅ Chat removed from your list.');
        currentChatId = null;
        document.getElementById('messages').innerHTML = `
          <div class="flex flex-col items-center justify-center h-full text-on-surface-variant/5 italic text-center" id="emptyStatePlaceholder">
            <p class="text-3xl font-light tracking-tighter opacity-10">ENCRYPTED LINK IDLE</p>
            <p class="text-[9px] mt-4 opacity-5 tracking-[0.4em] uppercase">Authorized personnel only</p>
          </div>
        `;
        document.getElementById('chatHeader').classList.add('hidden');
        await loadRecentChats();
      } else {
        const message = await res.text();
        showToast(`❌ ${message || 'Failed to purge conversation.'}`);
      }
    } catch (err) {
      console.error('Delete error:', err);
      showToast('❌ Terminal failure during purge.');
    }
  });

  // Group Creation Logic
  const createGroupModal = document.getElementById('createGroupModal');
  const openGroupBtn = document.getElementById('openCreateGroupModal');
  const closeGroupBtn = document.getElementById('closeCreateGroupModal');
  const groupMemberList = document.getElementById('groupMemberSearchList');
  const confirmGroupBtn = document.getElementById('confirmCreateGroup');
  const groupNameInput = document.getElementById('groupNameInput');
  
  let selectedMemberIds = new Set();

  openGroupBtn?.addEventListener('click', async () => {
    createGroupModal.classList.remove('hidden');
    selectedMemberIds.clear();
    await renderMemberSelectionList();
  });

  closeGroupBtn?.addEventListener('click', () => {
    createGroupModal.classList.add('hidden');
  });

  async function renderMemberSelectionList() {
    if (!allContacts || allContacts.length === 0) {
      const res = await fetch('/api/contacts/my', { headers });
      allContacts = await res.json();
    }

    if (allContacts.length === 0) {
      groupMemberList.innerHTML = '<p class="text-center py-10 text-on-surface-variant/30 italic">No contacts found to add.</p>';
      return;
    }

    groupMemberList.innerHTML = allContacts.map(contact => `
      <div class="flex items-center justify-between p-4 bg-surface-container rounded-2xl cursor-pointer hover:bg-primary/5 transition-all group border border-transparent hover:border-primary/20" data-member-id="${contact.id}">
        <div class="flex items-center gap-4">
          <div class="w-10 h-10 rounded-full overflow-hidden border border-outline-variant/30">
            <img src="${contact.profileImageUrl || 'https://ui-avatars.com/api/?name=' + (contact.name || contact.username) + '&background=random'}" class="w-full h-full object-cover">
          </div>
          <div>
            <div class="font-bold text-on-surface text-sm">${contact.name || contact.username}</div>
            <div class="text-[10px] text-on-surface-variant/50 uppercase tracking-widest">${contact.status || 'OFFLINE'}</div>
          </div>
        </div>
        <div class="selection-circle w-6 h-6 rounded-full border-2 border-outline-variant flex items-center justify-center transition-all group-hover:border-primary">
          <span class="material-symbols-outlined text-[16px] text-white hidden">check</span>
        </div>
      </div>
    `).join('');

    groupMemberList.querySelectorAll('[data-member-id]').forEach(item => {
      item.addEventListener('click', () => {
        const id = Number(item.getAttribute('data-member-id'));
        const circle = item.querySelector('.selection-circle');
        const check = circle.querySelector('span');

        if (selectedMemberIds.has(id)) {
          selectedMemberIds.delete(id);
          circle.classList.remove('bg-primary', 'border-primary');
          circle.classList.add('border-outline-variant');
          check.classList.add('hidden');
        } else {
          selectedMemberIds.add(id);
          circle.classList.remove('border-outline-variant');
          circle.classList.add('bg-primary', 'border-primary');
          check.classList.remove('hidden');
        }
      });
    });
  }

  confirmGroupBtn?.addEventListener('click', async () => {
    const name = groupNameInput.value.trim();
    if (!name) {
      showToast('⚠️ Identity required for group pulse.');
      return;
    }

    try {
      confirmGroupBtn.disabled = true;
      confirmGroupBtn.innerHTML = '<div class="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div> Launching...';
      
      const res = await fetch('/api/chats/group', {
        method: 'POST',
        headers,
        body: JSON.stringify({
          name: name,
          memberIds: Array.from(selectedMemberIds)
        })
      });

      if (res.ok) {
        showToast(`🚀 Group "${name}" launched!`);
        createGroupModal.classList.add('hidden');
        groupNameInput.value = '';
        selectedMemberIds.clear();
        await loadRecentChats(true);
      } else {
        showToast('❌ Launch sequence failed.');
      }
    } catch (err) {
      console.error('Create group error:', err);
      showToast('❌ Terminal failure during launch.');
    } finally {
      confirmGroupBtn.disabled = false;
      confirmGroupBtn.innerHTML = '<span class="material-symbols-outlined">rocket_launch</span> Launch Group';
    }
  });

  await loadCurrentUser();
  loadPinnedDmChats();
  await connectWebSocket();
  await loadPendingRequests();
  tabSwitch('chats'); // Init default tab
});

