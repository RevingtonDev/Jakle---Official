import React, { Component, createRef } from "react";
import { Helmet } from "react-helmet";
import { get_info, reset_password, update_account } from "../api/request";
import { PersonHeart } from "../components/Images";

export class Settings extends Component {
  constructor(props) {
    super(props);

    this.state = {
      user: {},
      editable: false,
      isWaiting: false,
      reset_output: undefined,
    };

    this.form = createRef();
  }

  setEditable(state) {
    if (!state) {
      const elms = this.form.current.elements;
      for (let i = 0; i < elms.length; i++) {
        elms[i].value = elms[i].defaultValue;
      }
    }
    this.setState({ editable: state });
  }

  async updateAccount() {
    this.setState({ isWaiting: true });
    let body = {},
      elms = this.form.current.elements;
    for (let i = 0; i < elms.length; i++) {
      body[elms[i].name] = elms[i].value;
    }
    const req = await update_account(JSON.stringify(body));
    if (req && req.code === 200) {
      this.setState({ user: body });
    } else {
      for (let i = 0; i < elms.length; i++) {
        elms[i].value = elms[i].defaultValue;
      }
    }
    this.setState({ isWaiting: false, editable: false });
  }

  async getInfo() {
    this.setState({ isWaiting: true });
    const req = await get_info("");
    if (req && req.code === 200) {
      this.setState({ user: req.results });
    }
    this.setState({ isWaiting: false });
  }

  async requestResetLink() {
    this.setState({ isWaiting: true, reset_output: undefined });
    const req = await reset_password();
    if (req) {
      if (req.code === 200) {
        this.setState({
          reset_output: {
            code: 200,
            msg: "A link has been emailed.",
          },
        });
      } else {
        this.setState({
          reset_output: {
            msg: req.query,
          },
        });
      }
    } else {
      this.setState({
        reset_output: {
          msg: "Something went wrong.",
        },
      });
    }
    console.log(this.state);
    this.setState({ isWaiting: false });
  }

  componentDidMount() {
    this.getInfo();
  }

  render() {
    const { user, editable, isWaiting, reset_output } = this.state;
    let date = undefined;
    if (user.dateOfBirth) {
      let _date = new Date(user.dateOfBirth),
        month = _date.getMonth() + 1;

      date = `${_date.getFullYear()}-${month < 10 ? `0${month}` : month}-${
        _date.getDate() < 10 ? `0${_date.getDate()}` : _date.getDate()
      }`;
    }
    return (
      <>
        <Helmet>
          <title>Jakle - Settings</title>
        </Helmet>
        <section className="page-container layout-page center justify-start column-flex">
          <div className="container page-container row">
            <div className="row center row-flex space-bet">
              <div className="container-title">
                Personal & Account Information
              </div>
              <button
                onClick={() => {
                  this.setEditable(true);
                }}
                className="btn action-btn"
                style={{ backgroundColor: "var(--fd-f)" }}
                disabled={isWaiting}
              >
                Edit
              </button>
            </div>
            <form
              ref={this.form}
              className="container-data center"
              onSubmit={(e) => {
                e.preventDefault();
              }}
            >
              <div
                className="profile-logo center editable row rel"
                style={{ padding: user.profilePic ? "0" : "30px" }}
              >
                {user && user.profilePic ? (
                  <img src={user.profilePic} alt="profile-pic" />
                ) : (
                  PersonHeart
                )}
              </div>
              <div className="user-info row">
                <div className="data-input data-show center column-flex align-start">
                  <label>Full Name</label>
                  <input
                    type="text"
                    name="name"
                    id="name"
                    defaultValue={user.name}
                    disabled={!editable || isWaiting}
                    spellCheck="false"
                    autoComplete="off"
                    required
                  />
                </div>
                <div className="data-input data-show center column-flex align-start">
                  <label>Date of Birth</label>
                  <input
                    type="date"
                    name="dateOfBirth"
                    disabled={!editable || isWaiting}
                    defaultValue={date}
                  />
                </div>
              </div>
            </form>
            {editable && (
              <div className="container-controlls row center flex-end">
                <button
                  onClick={() => {
                    this.updateAccount();
                  }}
                  type="submit"
                  className="btn action-btn"
                  style={{ backgroundColor: "green" }}
                  disabled={isWaiting}
                >
                  Save
                </button>
                <button
                  onClick={() => {
                    this.setEditable(false);
                  }}
                  className="btn action-btn"
                  style={{ backgroundColor: "red" }}
                  disabled={isWaiting}
                >
                  Cancel
                </button>
              </div>
            )}
          </div>
          <div className="container row">
            <div className="container-title">Privacy and Security</div>
            <div className="container-data" style={{ padding: "15px 20px" }}>
              <div className="data-field row center justify-start">
                <div className="row">
                  <label style={{ marginRight: "20px" }}>Password</label>
                  <button
                    onClick={() => {
                      this.requestResetLink();
                    }}
                    className="btn action-btn"
                    style={{ backgroundColor: "var(--fd-f)" }}
                    disabled={isWaiting}
                  >
                    Reset Password
                  </button>
                </div>
                {reset_output && (
                  <>
                    <span
                      style={{ justifySelf: "flex-end", marginLeft: "auto" }}
                      className={
                        "info " +
                        (reset_output.code === 200 ? "des" : "des-err")
                      }
                    >
                      {reset_output.msg}
                    </span>
                  </>
                )}
              </div>
            </div>
          </div>
        </section>
      </>
    );
  }
}
