(function () {
  function syncMobileTabState(tab, mobileTabButtons) {
    if (!mobileTabButtons) return;
    mobileTabButtons.forEach((btn) => {
      const isActive = btn.dataset.mobileTab === tab;
      btn.classList.toggle('text-cyan-400', isActive);
      btn.classList.toggle('text-slate-500', !isActive);
      const icon = btn.querySelector('.material-symbols-outlined');
      if (icon) {
        icon.style.fontVariationSettings = isActive ? "'FILL' 1" : "'FILL' 0";
      }
    });
  }

  function bindBottomNav(mobileBottomNav, tabSwitch) {
    mobileBottomNav?.addEventListener('click', (e) => {
      const mobileTabBtn = e.target.closest('button[data-mobile-tab]');
      if (!mobileTabBtn) return;
      const tab = mobileTabBtn.dataset.mobileTab;
      if (!tab) return;
      tabSwitch(tab);
    });
  }

  function bindBackButton(getCurrentTab) {
    const mobileBackBtn = document.getElementById('mobileBackBtn');
    if (!mobileBackBtn) return;

    mobileBackBtn.addEventListener('click', () => {
      const rightSection = document.getElementById('rightSection');
      if (!rightSection || window.innerWidth >= 768) return;

      rightSection.classList.add('hidden');
      rightSection.classList.remove('flex');

      const middleSection = document.getElementById('middleSection');
      const leftSection = document.getElementById('contactsSection');
      if (getCurrentTab() === 'contacts' && leftSection) {
        leftSection.classList.remove('hidden');
        leftSection.classList.add('flex');
      } else if (middleSection) {
        middleSection.classList.remove('hidden');
        middleSection.classList.add('flex');
      }
    });
  }

  window.NxMobileNav = {
    create: function createMobileNav(options) {
      const mobileBottomNav = options.mobileBottomNav;
      const mobileTabButtons = options.mobileTabButtons;
      const tabSwitch = options.tabSwitch;
      const getCurrentTab = options.getCurrentTab;

      return {
        syncTabState: function (tab) {
          syncMobileTabState(tab, mobileTabButtons);
        },
        bindEvents: function () {
          bindBottomNav(mobileBottomNav, tabSwitch);
          bindBackButton(getCurrentTab);
        }
      };
    }
  };
})();
