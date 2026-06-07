import { Controller } from "@hotwired/stimulus";

export default class extends Controller {
  static targets = ["button", "panel"];
  static values = { active: String };

  connect() {
    const initial = this.activeValue || this.buttonTargets[0]?.dataset.tab;
    this.show(initial);
  }

  switch(event) {
    event.preventDefault();
    this.show(event.currentTarget.dataset.tab);
  }

  show(name) {
    if (!name) return;

    this.buttonTargets.forEach((button) => {
      const active = button.dataset.tab === name;
      button.classList.toggle("tabs__button--active", active);
      button.setAttribute("aria-selected", active);
    });

    this.panelTargets.forEach((panel) => {
      panel.hidden = panel.dataset.tabPanel !== name;
    });
  }
}
