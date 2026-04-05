(function () {
  window.NxGifModule = {
    create: function createGifModule(options) {
      const gifPrefix = options.gifPrefix || '__GIF__:';
      const gifPickerModal = options.gifPickerModal;
      const closeGifPickerModal = options.closeGifPickerModal;
      const gifSearchInput = options.gifSearchInput;
      const gifResults = options.gifResults;
      const plusBtn = options.plusBtn;
      const sendChatContent = options.sendChatContent;
      const escapeHtml = options.escapeHtml;
      const debounce = options.debounce;

      function isGifMessage(content) {
        return typeof content === 'string' && content.startsWith(gifPrefix);
      }

      function extractGifUrl(content) {
        return isGifMessage(content) ? content.slice(gifPrefix.length).trim() : '';
      }

      function openGifModal() {
        gifPickerModal?.classList.remove('hidden');
        if (gifSearchInput) {
          gifSearchInput.focus();
        }
      }

      function closeGifModal() {
        gifPickerModal?.classList.add('hidden');
      }

      async function loadGifResults(query) {
        if (!gifResults) return;
        const trimmed = (query || '').trim();
        if (!trimmed) {
          gifResults.innerHTML = '<div class="col-span-full text-center text-on-surface-variant py-8">Search to load GIFs</div>';
          return;
        }

        gifResults.innerHTML = '<div class="col-span-full text-center text-on-surface-variant py-8">Loading GIFs...</div>';

        try {
          const endpoint = 'https://tenor.googleapis.com/v2/search?q=' + encodeURIComponent(trimmed) + '&key=LIVDSRZULELA&client_key=nxchat&limit=18&media_filter=gif';
          const res = await fetch(endpoint);
          if (!res.ok) {
            throw new Error('GIF search unavailable');
          }

          const data = await res.json();
          const items = Array.isArray(data.results) ? data.results : [];
          if (!items.length) {
            gifResults.innerHTML = '<div class="col-span-full text-center text-on-surface-variant py-8">No GIFs found for this search</div>';
            return;
          }

          const cards = items
            .map((item) => {
              const url = item?.media_formats?.gif?.url || item?.media_formats?.tinygif?.url;
              if (!url) return '';
              return '\n            <button class="gif-item-btn rounded-xl overflow-hidden border border-outline-variant/30 hover:border-primary transition-all" data-gif-url="' + escapeHtml(url) + '">\n              <img src="' + escapeHtml(url) + '" alt="GIF option" class="w-full h-32 object-cover" loading="lazy" />\n            </button>\n          ';
            })
            .filter(Boolean)
            .join('');

          gifResults.innerHTML = cards || '<div class="col-span-full text-center text-on-surface-variant py-8">No GIFs available</div>';

          gifResults.querySelectorAll('.gif-item-btn').forEach((btn) => {
            btn.addEventListener('click', () => {
              const gifUrl = btn.getAttribute('data-gif-url');
              if (!gifUrl) return;
              sendChatContent(gifPrefix + gifUrl);
              closeGifModal();
            });
          });
        } catch (err) {
          console.error('GIF search error:', err);
          gifResults.innerHTML = '<div class="col-span-full text-center text-error py-8">Failed to load GIFs. Please try again.</div>';
        }
      }

      function bindEvents() {
        plusBtn?.addEventListener('click', openGifModal);
        closeGifPickerModal?.addEventListener('click', closeGifModal);
        gifPickerModal?.addEventListener('click', (e) => {
          if (e.target === gifPickerModal) {
            closeGifModal();
          }
        });
        gifSearchInput?.addEventListener('input', debounce((e) => {
          loadGifResults(e.target.value);
        }, 300));
      }

      return {
        isGifMessage: isGifMessage,
        extractGifUrl: extractGifUrl,
        openGifModal: openGifModal,
        closeGifModal: closeGifModal,
        loadGifResults: loadGifResults,
        bindEvents: bindEvents
      };
    }
  };
})();
