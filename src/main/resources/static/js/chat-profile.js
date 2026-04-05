(function () {
  window.NxProfileModule = {
    create: function createProfileModule(options) {
      const headers = options.headers;
      const profileContent = options.profileContent;
      const showToast = options.showToast;
      const escapeHtml = options.escapeHtml;
      const getCurrentUserData = options.getCurrentUserData;
      const setCurrentUserData = options.setCurrentUserData;
      const onProfileUpdated = options.onProfileUpdated;

      async function loadCurrentUser() {
        try {
          const res = await fetch('/api/users/me', { headers: headers });
          const user = await res.json();
          setCurrentUserData(user);
          renderProfile();

          const profileName = document.getElementById('profileName');
          if (profileName && user && user.username) {
            profileName.textContent = user.username;
          }
        } catch (err) {
          console.error('Load user error:', err);
        }
      }

      function renderProfile() {
        const currentUserData = getCurrentUserData();
        if (!currentUserData || !profileContent) return;

        profileContent.innerHTML = '\n      <div class="text-center mb-6">\n        <div class="w-24 h-24 rounded-full bg-primary mx-auto mb-4 flex items-center justify-center text-4xl font-bold text-on-primary">\n          ' + (currentUserData.username?.charAt(0).toUpperCase() || '?') + '\n        </div>\n        <h4 class="text-xl font-bold">' + (currentUserData.username || 'User') + '</h4>\n        <p class="text-on-surface-variant">' + currentUserData.email + '</p>\n      </div>\n      <form id="changePasswordForm" class="space-y-4">\n        <div>\n          <label class="block text-sm font-medium mb-2 text-on-surface-variant">Username</label>\n          <input type="text" id="newUsername" value="' + escapeHtml(currentUserData.username || '') + '" placeholder="Enter username" class="w-full bg-surface-container rounded-xl px-4 py-3 border border-outline-variant text-on-surface placeholder:text-on-surface-variant focus:ring-2 focus:ring-primary focus:border-transparent">\n        </div>\n        <div>\n          <label class="block text-sm font-medium mb-2 text-on-surface-variant">New Password</label>\n          <input type="password" id="newPassword" placeholder="Enter new password" class="w-full bg-surface-container rounded-xl px-4 py-3 border border-outline-variant text-on-surface placeholder:text-on-surface-variant focus:ring-2 focus:ring-primary focus:border-transparent">\n        </div>\n        <button type="submit" class="w-full bg-primary text-on-primary-fixed py-3 rounded-xl font-bold hover:bg-primary/90 transition-colors">Update Profile</button>\n      </form>\n    ';

        attachPasswordFormHandler();
      }

      function attachPasswordFormHandler() {
        const form = document.getElementById('changePasswordForm');
        if (!form) return;

        form.addEventListener('submit', async (e) => {
          e.preventDefault();
          const currentUserData = getCurrentUserData();
          const password = document.getElementById('newPassword').value;
          const username = document.getElementById('newUsername')?.value?.trim();

          const payload = {};
          if (username && username !== currentUserData.username) {
            payload.username = username;
          }
          if (password) {
            payload.password = password;
          }

          if (Object.keys(payload).length === 0) {
            showToast('⚠️ No profile changes to update.');
            return;
          }

          try {
            const res = await fetch('/api/users/' + currentUserData.id, {
              method: 'PUT',
              headers: headers,
              body: JSON.stringify(payload)
            });

            if (!res.ok) {
              const message = await res.text();
              throw new Error(message || 'Failed to update profile');
            }

            const updated = await res.json();
            const merged = { ...currentUserData, ...updated };
            setCurrentUserData(merged);

            const existingLocalUser = JSON.parse(localStorage.getItem('user') || '{}');
            localStorage.setItem('user', JSON.stringify({ ...existingLocalUser, ...updated }));

            const profileName = document.getElementById('profileName');
            if (profileName && merged.username) {
              profileName.textContent = merged.username;
            }
            if (typeof onProfileUpdated === 'function') {
              onProfileUpdated(merged);
            }

            showToast('✅ Profile updated successfully');
            document.getElementById('closeProfileModal')?.click();
          } catch (err) {
            console.error('Profile update error:', err);
            showToast('❌ ' + (err.message || 'Failed to update profile'));
          }
        });
      }

      return {
        loadCurrentUser: loadCurrentUser,
        renderProfile: renderProfile
      };
    }
  };
})();
