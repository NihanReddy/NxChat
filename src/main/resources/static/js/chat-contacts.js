(function () {
  window.NxContactsModule = {
    create: function createContactsModule(options) {
      const headers = options.headers;
      const showToast = options.showToast;
      const renderCurrentList = options.renderCurrentList;
      const getCurrentTab = options.getCurrentTab;
      const setAllContacts = options.setAllContacts;
      const getPendingRequests = options.getPendingRequests;
      const setPendingRequests = options.setPendingRequests;
      const loadRecentChats = options.loadRecentChats;
      const tabSwitch = options.tabSwitch;
      const selectChat = options.selectChat;

      const requestList = options.requestList;
      const requestsContainer = options.requestsContainer;
      const addContactModal = options.addContactModal;
      const addContactBtn = options.addContactBtn;
      const addContactNoContactsBtn = options.addContactNoContactsBtn;
      const closeAddContactModal = options.closeAddContactModal;
      const confirmAddContact = options.confirmAddContact;
      const addContactEmailInput = options.addContactEmailInput;
      const toggleContacts = options.toggleContacts;
      const contactsContainer = options.contactsContainer;
      const toggleRequests = options.toggleRequests;
      const dismissBanner = options.dismissBanner;

      async function loadContacts() {
        try {
          const res = await fetch('/api/contacts/my', { headers: headers });
          if (!res.ok) throw new Error('Unable to fetch contacts');
          const contacts = await res.json();
          setAllContacts(Array.isArray(contacts) ? contacts : []);
          renderCurrentList();
        } catch (err) {
          console.error('loadContacts', err);
          const leftList = document.getElementById('leftList');
          if (leftList) {
            leftList.innerHTML = '<div class="py-12 text-center text-on-surface-variant">Could not load contacts.</div>';
          }
        }
      }

      async function loadPendingRequests() {
        try {
          const res = await fetch('/api/contacts/pending', { headers: headers });
          if (!res.ok) throw new Error('Unable to fetch requests');
          const requests = await res.json();
          setPendingRequests(Array.isArray(requests) ? requests : []);
          updatePendingBadge(getPendingRequests().length);
          renderRequests();
        } catch (err) {
          console.error('loadPendingRequests', err);
          if (requestList) {
            requestList.innerHTML = '<div class="py-12 text-center text-on-surface-variant">No pending requests.</div>';
          }
          updatePendingBadge(0);
        }
      }

      function updatePendingBadge(count) {
        const badge = document.getElementById('pendingBadge');
        if (badge) {
          if (count > 0) {
            badge.textContent = count;
            badge.classList.remove('hidden');
          } else {
            badge.classList.add('hidden');
          }
        }
        const banner = document.getElementById('pendingRequestsBanner');
        if (banner) {
          if (count > 0 && getCurrentTab() === 'chats') {
            banner.classList.remove('hidden');
          } else {
            banner.classList.add('hidden');
          }
        }
      }

      function renderRequests() {
        const pendingRequests = getPendingRequests();
        if (!pendingRequests.length) {
          if (requestList) {
            requestList.innerHTML = '<div class="py-12 text-center text-on-surface-variant text-sm italic">Atmosphere is currently clear of pending connection requests</div>';
          }
          if (requestsContainer) {
            requestsContainer.classList.add('max-h-0', 'opacity-50');
          }
          return;
        }

        if (requestsContainer) {
          requestsContainer.classList.remove('max-h-0', 'opacity-50');
        }

        requestList.innerHTML = pendingRequests.map((request) => {
          if (!request || !request.owner) return '';
          const senderName = request.owner.username || request.owner.email;
          return '\n        <div class="flex items-center justify-between p-4 rounded-xl bg-white/5 hover:bg-white/10 transition-all border border-white/5">\n          <div class="flex items-center gap-4 flex-1 min-w-0">\n            <div class="w-10 h-10 rounded-full overflow-hidden shrink-0 ring-2 ring-primary/20" style="background-image: url(\'https://ui-avatars.com/api/?name=' + encodeURIComponent(senderName) + '&background=8ff5ff&color=001115\')"></div>\n            <div class="min-w-0 flex-1">\n              <div class="font-bold text-on-surface truncate text-sm">' + senderName + '</div>\n              <p class="text-[10px] text-on-surface-variant uppercase tracking-wider">Incoming Handshake</p>\n            </div>\n          </div>\n          <div class="flex gap-2 ml-4">\n            <button onclick="respondToRequest(' + request.id + ', \'DENY\')" class="p-2 text-on-surface-variant hover:text-error transition-all"><span class="material-symbols-outlined text-lg">cancel</span></button>\n            <button onclick="respondToRequest(' + request.id + ', \'ACCEPT\')" class="p-2 bg-primary/20 text-primary rounded-full hover:bg-primary hover:text-on-primary-fixed transition-all transition-all"><span class="material-symbols-outlined text-lg">check_circle</span></button>\n          </div>\n        </div>\n      ';
        }).join('');
      }

      async function respondToRequest(requestId, action) {
        try {
          const res = await fetch('/api/contacts/respond', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({ requestId: Number(requestId), action: action })
          });
          const data = await res.json();
          if (res.ok) {
            await loadPendingRequests();
            await loadContacts();
            if (data.chatId) {
              await loadRecentChats(true);
              tabSwitch('chats');
              selectChat(data.chatId);
            } else {
              await loadRecentChats(true);
            }
          } else {
            showToast('⚠️ ' + (data.message || 'Response failed'));
          }
        } catch (err) {
          console.error('respondToRequest', err);
          showToast('⚠️ Error responding to request');
        }
      }

      function bindWindowActions() {
        window.respondToRequest = respondToRequest;
      }

      function bindPanelEvents() {
        toggleContacts?.addEventListener('click', () => {
          contactsContainer.classList.toggle('h-[60vh]');
          contactsContainer.classList.toggle('max-h-0');
          const icon = toggleContacts.querySelector('span.material-symbols-outlined');
          icon.textContent = contactsContainer.classList.contains('max-h-0') ? 'expand_more' : 'expand_less';
        });

        toggleRequests?.addEventListener('click', () => {
          requestsContainer.classList.toggle('h-[30vh]');
          requestsContainer.classList.toggle('max-h-0');
          const icon = toggleRequests.querySelector('span.material-symbols-outlined');
          icon.textContent = requestsContainer.classList.contains('max-h-0') ? 'expand_more' : 'expand_less';
        });

        dismissBanner?.addEventListener('click', () => {
          const banner = document.getElementById('pendingRequestsBanner');
          if (banner) banner.classList.add('hidden');
        });
      }

      function bindAddContactEvents() {
        addContactBtn?.addEventListener('click', () => {
          addContactModal?.classList.remove('hidden');
        });

        addContactNoContactsBtn?.addEventListener('click', () => {
          addContactModal?.classList.remove('hidden');
        });

        closeAddContactModal?.addEventListener('click', () => {
          addContactModal?.classList.add('hidden');
        });

        confirmAddContact?.addEventListener('click', async () => {
          const identifier = addContactEmailInput.value.trim();
          if (!identifier) {
            showToast('⚠️ Target email or mobile number required.');
            return;
          }

          try {
            confirmAddContact.disabled = true;
            confirmAddContact.innerHTML = '<div class="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div> Transmitting...';

            const res = await fetch('/api/contacts/request', {
              method: 'POST',
              headers: headers,
              body: JSON.stringify({ identifier: identifier })
            });

            if (res.ok) {
              showToast('🚀 Connection request transmitted!');
              addContactModal?.classList.add('hidden');
              addContactEmailInput.value = '';
            } else {
              const data = await res.json();
              showToast('❌ Connection failed: ' + (data.message || 'Identity not found'));
            }
          } catch (err) {
            console.error('Add contact error:', err);
            showToast('❌ Transmission failure.');
          } finally {
            confirmAddContact.disabled = false;
            confirmAddContact.innerHTML = '<span class="material-symbols-outlined">person_add</span> Establish Connection';
          }
        });
      }

      return {
        loadContacts: loadContacts,
        loadPendingRequests: loadPendingRequests,
        updatePendingBadge: updatePendingBadge,
        renderRequests: renderRequests,
        bindWindowActions: bindWindowActions,
        bindPanelEvents: bindPanelEvents,
        bindAddContactEvents: bindAddContactEvents
      };
    }
  };
})();
