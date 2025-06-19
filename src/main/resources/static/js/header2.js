// Header scroll effect
function updateHeaderState() {
    const header = document.querySelector('.header');
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;

    if (window.innerWidth > 768) {
        if (scrollTop > 50) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
    }
}

window.addEventListener('scroll', updateHeaderState);
window.addEventListener('resize', updateHeaderState);
document.addEventListener('DOMContentLoaded', updateHeaderState);

// Mobile user menu toggle
document.addEventListener('DOMContentLoaded', function() {
    const userMenu = document.querySelector('.user-menu');
    const overlay = document.querySelector('.mobile-overlay');
    const body = document.body;

    function isMobile() {
        return window.innerWidth <= 768;
    }

    function toggleMobileMenu() {
        if (isMobile()) {
            userMenu.classList.toggle('active');
            overlay.classList.toggle('active');
            body.style.overflow = userMenu.classList.contains('active') ? 'hidden' : '';
        }
    }

    function closeMobileMenu() {
        userMenu.classList.remove('active');
        overlay.classList.remove('active');
        body.style.overflow = '';
    }

    // User menu click
    userMenu.addEventListener('click', function(e) {
        if (isMobile()) {
            e.stopPropagation();
            toggleMobileMenu();
        }
    });

    // Overlay click
    overlay.addEventListener('click', closeMobileMenu);

    // Close on window resize
    window.addEventListener('resize', function() {
        if (!isMobile()) {
            closeMobileMenu();
        }
    });

    // Close on escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeMobileMenu();
        }
    });

    // Close mobile menu when clicking dropdown links
    document.querySelectorAll('.user-dropdown-link').forEach(link => {
        link.addEventListener('click', function() {
            if (isMobile()) {
                closeMobileMenu();
            }
        });
    });
});

// Search functionality
document.querySelector('.search-input').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        const query = this.value.trim();
        if (query) {
            // Implement search functionality
            console.log('Search for:', query);
        }
    }
});