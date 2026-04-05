(function () {
  window.NxHeaderPinModule = {
    create: function createHeaderPinModule(options) {
      const currentUser = options.currentUser;
      const pinChatBtn = options.pinChatBtn;
      const isGroupChat = options.isGroupChat;
      const showToast = options.showToast;
      const renderCurrentList = options.renderCurrentList;
      const getCurrentChatId = options.getCurrentChatId;
      const getCurrentChatMeta = options.getCurrentChatMeta;

      let pinnedDmChatIds = new Set();

      function getPinnedChatsStorageKey() {
        return 'pinnedDmChats_' + (currentUser?.id || 'anon');
      }

      function loadPinnedDmChats() {
        try {
          const raw = localStorage.getItem(getPinnedChatsStorageKey());
          const parsed = JSON.parse(raw || '[]');
          pinnedDmChatIds = new Set(Array.isArray(parsed) ? parsed.map(Number).filter(Number.isFinite) : []);
        } catch {
          pinnedDmChatIds = new Set();
        }
      }

      function persistPinnedDmChats() {
        localStorage.setItem(getPinnedChatsStorageKey(), JSON.stringify(Array.from(pinnedDmChatIds)));
      }

      function isPinnedDm(chat) {
        if (!chat || isGroupChat(chat)) return false;
        return pinnedDmChatIds.has(Number(chat.chatId));
      }

      function updateChatHeader(chat) {
        const header = document.getElementById('chatHeader');
        if (!header) return;

        header.classList.remove('hidden');

        const avatarImg = document.getElementById('headerAvatar');
        const nameEl = document.getElementById('headerName');
        const statusText = document.getElementById('headerStatusText');
        const dot = document.getElementById('headerStatusDot');
        const isGroup = isGroupChat(chat);
        const isPinned = isPinnedDm(chat);

        const isDeleted = chat.recipientDeleted === true || chat.recipientStatus === 'DELETED';
        const displayName = isDeleted ? 'Deleted Account' : (chat.recipientName || 'Unknown');

        if (avatarImg) {
          avatarImg.src = chat.recipientAvatarUrl || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(displayName) + '&background=8ff5ff&color=001115';
        }
        if (nameEl) {
          nameEl.textContent = displayName;
          if (isGroup) {
            nameEl.classList.add('cursor-pointer', 'hover:text-primary');
            nameEl.title = 'Click to view group members';
          } else {
            nameEl.classList.remove('cursor-pointer', 'hover:text-primary');
            nameEl.removeAttribute('title');
          }
        }

        const isOnline = !isDeleted && (chat.recipientStatus || 'OFFLINE') === 'ONLINE';
        if (statusText) {
          statusText.textContent = isDeleted ? 'Account Deleted' : (isOnline ? 'Online' : 'Offline');
          statusText.className = 'text-[10px] font-bold uppercase tracking-widest ' + (isDeleted ? 'text-error' : (isOnline ? 'text-cyan-400' : 'text-on-surface-variant/40'));
        }
        if (dot) {
          dot.className = 'w-1.5 h-1.5 rounded-full ' + (isDeleted ? 'bg-error' : (isOnline ? 'bg-cyan-500 shadow-[0_0_8px_rgba(6,182,212,0.8)]' : 'bg-on-surface-variant/20'));
        }

        if (pinChatBtn) {
          if (isGroup) {
            pinChatBtn.classList.add('hidden');
          } else {
            pinChatBtn.classList.remove('hidden');
            pinChatBtn.title = isPinned ? 'Unpin DM chat' : 'Pin DM chat';
            pinChatBtn.classList.toggle('text-primary', isPinned);
            pinChatBtn.classList.toggle('bg-primary/10', isPinned);
            const pinIcon = pinChatBtn.querySelector('.material-symbols-outlined');
            if (pinIcon) {
              pinIcon.textContent = isPinned ? 'keep_off' : 'keep';
            }
          }
        }
      }

      function togglePinCurrentChat() {
        const currentChatMeta = getCurrentChatMeta();
        if (!currentChatMeta || isGroupChat(currentChatMeta)) {
          showToast('⚠️ Only DM chats can be pinned.');
          return;
        }

        const chatId = Number(currentChatMeta.chatId || getCurrentChatId());
        if (!chatId) return;

        if (pinnedDmChatIds.has(chatId)) {
          pinnedDmChatIds.delete(chatId);
          showToast('📌 DM chat unpinned.');
        } else {
          pinnedDmChatIds.add(chatId);
          showToast('📌 DM chat pinned to top.');
        }

        persistPinnedDmChats();
        updateChatHeader(currentChatMeta);
        renderCurrentList();
      }

      function bindEvents() {
        pinChatBtn?.addEventListener('click', togglePinCurrentChat);
      }

      return {
        loadPinnedDmChats: loadPinnedDmChats,
        isPinnedDm: isPinnedDm,
        updateChatHeader: updateChatHeader,
        togglePinCurrentChat: togglePinCurrentChat,
        bindEvents: bindEvents
      };
    }
  };
})();
