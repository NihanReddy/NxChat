(function () {
  window.NxGroupModule = {
    create: function createGroupModule(options) {
      const headers = options.headers;
      const showToast = options.showToast;
      const escapeHtml = options.escapeHtml;
      const isGroupChat = options.isGroupChat;
      const currentUser = options.currentUser;
      const loadRecentChats = options.loadRecentChats;
      const loadContacts = options.loadContacts;
      const getAllContacts = options.getAllContacts;
      const getCurrentChatId = options.getCurrentChatId;
      const getCurrentChatMeta = options.getCurrentChatMeta;
      const clearCurrentChat = options.clearCurrentChat;
      const onOpenDmDetails = options.onOpenDmDetails;

      const groupMembersModal = options.groupMembersModal;
      const groupMembersList = options.groupMembersList;
      const groupAddCandidates = options.groupAddCandidates;
      const groupAdminPanel = options.groupAdminPanel;
      const groupMembersSubtitle = options.groupMembersSubtitle;
      const deleteGroupBtn = options.deleteGroupBtn;

      async function openGroupMembersModal() {
        const currentChatId = getCurrentChatId();
        const currentChatMeta = getCurrentChatMeta();
        if (!currentChatId || !isGroupChat(currentChatMeta)) return;

        groupMembersModal?.classList.remove('hidden');
        if (groupMembersList) {
          groupMembersList.innerHTML = '<div class="py-6 text-center text-on-surface-variant">Loading group members...</div>';
        }
        if (groupAddCandidates) {
          groupAddCandidates.innerHTML = '';
        }

        try {
          const res = await fetch('/api/chats/' + currentChatId + '/members', { headers: headers });
          if (!res.ok) {
            const msg = await res.text();
            throw new Error(msg || 'Unable to fetch members (' + res.status + ')');
          }

          const members = await res.json();
          const me = members.find((m) => Number(m.userId) === Number(currentUser.id));
          const isAdmin = me?.memberRole === 'ADMIN';

          if (groupMembersSubtitle) {
            groupMembersSubtitle.textContent = isAdmin
              ? 'You are admin. You can add or remove members.'
              : 'Members in this group';
          }

          renderGroupMembersList(members, isAdmin);

          if (isAdmin) {
            await renderAddCandidatesForGroup(members);
            groupAdminPanel?.classList.remove('hidden');
          } else {
            groupAdminPanel?.classList.add('hidden');
          }
        } catch (err) {
          console.error('Open group members modal error:', err);
          if (groupMembersList) {
            groupMembersList.innerHTML = '<div class="py-6 text-center text-error">' + escapeHtml(err.message || 'Failed to load group members') + '</div>';
          }
          groupAdminPanel?.classList.add('hidden');
        }
      }

      function renderGroupMembersList(members, isAdmin) {
        if (!groupMembersList) return;

        if (!Array.isArray(members) || members.length === 0) {
          groupMembersList.innerHTML = '<div class="py-6 text-center text-on-surface-variant">No members found.</div>';
          return;
        }

        groupMembersList.innerHTML = members.map((member) => {
          const displayName = member.deleted ? 'Deleted Account' : (member.name || member.username || member.email || 'Member');
          const role = member.memberRole || 'MEMBER';
          const canRemove = isAdmin && role !== 'ADMIN' && Number(member.userId) !== Number(currentUser.id);
          const avatar = member.profileImageUrl || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(displayName) + '&background=8ff5ff&color=001115';
          const roleChipClass = role === 'ADMIN' ? 'text-secondary border-secondary/40 bg-secondary/10' : 'text-on-surface-variant border-outline-variant/50 bg-surface-container';

          return '\n        <div class="flex items-center justify-between p-3 rounded-xl bg-surface-container border border-outline-variant/20" data-group-member-id="' + member.userId + '">\n          <div class="flex items-center gap-3 min-w-0">\n            <img src="' + avatar + '" alt="' + escapeHtml(displayName) + '" class="w-10 h-10 rounded-full object-cover">\n            <div class="min-w-0">\n              <div class="font-semibold text-on-surface truncate">' + escapeHtml(displayName) + '</div>\n              <div class="text-[10px] uppercase tracking-widest text-on-surface-variant">' + (member.status || 'OFFLINE') + '</div>\n            </div>\n          </div>\n          <div class="flex items-center gap-2 ml-3">\n            <span class="text-[10px] uppercase tracking-wider px-2 py-1 rounded-full border ' + roleChipClass + '">' + role + '</span>\n            ' + (canRemove ? '<button class="remove-group-member-btn w-8 h-8 rounded-full flex items-center justify-center text-error hover:bg-error/10" data-remove-user-id="' + member.userId + '" title="Remove member"><span class="material-symbols-outlined text-base">person_remove</span></button>' : '') + '\n          </div>\n        </div>\n      ';
        }).join('');

        groupMembersList.querySelectorAll('.remove-group-member-btn').forEach((btn) => {
          btn.addEventListener('click', async () => {
            const userId = Number(btn.getAttribute('data-remove-user-id'));
            const ok = confirm('Remove this member from the group?');
            if (!ok) return;

            try {
              const res = await fetch('/api/chats/' + getCurrentChatId() + '/members/' + userId, { method: 'DELETE', headers: headers });
              if (!res.ok) {
                const msg = await res.text();
                throw new Error(msg || 'Failed to remove member');
              }
              showToast('✅ Member removed from group.');
              await openGroupMembersModal();
            } catch (err) {
              console.error('Remove group member error:', err);
              showToast('❌ ' + (err.message || 'Failed to remove member'));
            }
          });
        });
      }

      async function renderAddCandidatesForGroup(members) {
        if (!groupAddCandidates) return;

        let contacts = getAllContacts();
        if (!contacts || contacts.length === 0) {
          await loadContacts();
          contacts = getAllContacts();
        }

        const memberIds = new Set((members || []).map((m) => Number(m.userId)));
        const candidates = (contacts || []).filter((c) => !memberIds.has(Number(c.id)));

        if (candidates.length === 0) {
          groupAddCandidates.innerHTML = '<div class="text-xs text-on-surface-variant py-2">No available contacts to add.</div>';
          return;
        }

        groupAddCandidates.innerHTML = candidates.map((c) => {
          const displayName = c.name || c.username || c.email || 'Contact';
          return '\n        <div class="flex items-center justify-between gap-3 p-2 rounded-lg bg-surface-container border border-outline-variant/20">\n          <div class="min-w-0">\n            <div class="text-sm font-semibold text-on-surface truncate">' + escapeHtml(displayName) + '</div>\n            <div class="text-[10px] text-on-surface-variant truncate">' + escapeHtml(c.email || '') + '</div>\n          </div>\n          <button class="add-group-member-btn px-3 py-1.5 rounded-full text-xs font-bold bg-primary/15 text-primary hover:bg-primary/25" data-add-user-id="' + c.id + '">Add</button>\n        </div>\n      ';
        }).join('');

        groupAddCandidates.querySelectorAll('.add-group-member-btn').forEach((btn) => {
          btn.addEventListener('click', async () => {
            const userId = Number(btn.getAttribute('data-add-user-id'));
            try {
              const res = await fetch('/api/chats/' + getCurrentChatId() + '/members', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ userId: userId })
              });
              if (!res.ok) {
                const msg = await res.text();
                throw new Error(msg || 'Failed to add member');
              }
              showToast('✅ Member added to group.');
              await openGroupMembersModal();
            } catch (err) {
              console.error('Add group member error:', err);
              showToast('❌ ' + (err.message || 'Failed to add member'));
            }
          });
        });
      }

      async function handleDeleteGroup() {
        const currentChatId = getCurrentChatId();
        const currentChatMeta = getCurrentChatMeta();
        if (!currentChatId || !isGroupChat(currentChatMeta)) return;
        const confirmed = confirm('Delete this group permanently? This action cannot be undone.');
        if (!confirmed) return;

        try {
          const res = await fetch('/api/chats/' + currentChatId, { method: 'DELETE', headers: headers });
          if (!res.ok) {
            const message = await res.text();
            throw new Error(message || 'Failed to delete group');
          }

          showToast('✅ Group removed from your chat list.');
          groupMembersModal?.classList.add('hidden');
          clearCurrentChat();

          const header = document.getElementById('chatHeader');
          if (header) header.classList.add('hidden');
          const messagesEl = document.getElementById('messages');
          if (messagesEl) {
            messagesEl.innerHTML = '\n          <div class="flex flex-col items-center justify-center h-full text-on-surface-variant/10 italic text-center" id="emptyStatePlaceholder">\n            <p class="text-3xl font-light tracking-tighter opacity-20">ENCRYPTED LINK IDLE</p>\n            <p class="text-[10px] mt-2 opacity-10 tracking-[0.3em] uppercase transition-all duration-700">Select frequency to begin</p>\n          </div>\n        ';
          }

          await loadRecentChats(true);
        } catch (err) {
          console.error('Delete group error:', err);
          showToast('❌ ' + (err.message || 'Failed to delete group.'));
        }
      }

      function bindEvents() {
        document.getElementById('headerName')?.addEventListener('click', () => {
          if (isGroupChat(getCurrentChatMeta())) {
            openGroupMembersModal();
          } else if (typeof onOpenDmDetails === 'function') {
            onOpenDmDetails();
          }
        });
        document.getElementById('closeGroupMembersModal')?.addEventListener('click', () => {
          groupMembersModal?.classList.add('hidden');
        });
        deleteGroupBtn?.addEventListener('click', handleDeleteGroup);
      }

      return {
        openGroupMembersModal: openGroupMembersModal,
        handleDeleteGroup: handleDeleteGroup,
        bindEvents: bindEvents
      };
    }
  };
})();
