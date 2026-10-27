import { renderFlyerGenerator } from './flyerGenerator.js';
import { renderFrameMaker } from './frameMaker.js';
import { renderCalendarModule } from './calendarModule.js';

export function renderPSTDashboard(container, user, activeSubmodule = 'notices') {
  if (activeSubmodule === 'greetings') {
    renderFrameMaker(container, user);
    return;
  }
  if (activeSubmodule === 'calendar') {
    renderCalendarModule(container, user, (dest) => {
      renderPSTDashboard(container, user, dest);
    });
    return;
  }

  // Default: Notice & Event Flyers
  renderFlyerGenerator(container, user);
}
